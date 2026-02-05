package com.xdw.demobackend.service.statistics;

import com.xdw.demobackend.dto.statistics.*;
import com.xdw.demobackend.entity.*;
import com.xdw.demobackend.repository.ExpenseParticipantRepository;
import com.xdw.demobackend.repository.ExpenseRecordRepository;
import com.xdw.demobackend.repository.LedgerMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.tuple.Tuple2;
import org.babyfish.jimmer.sql.ast.tuple.Tuple3;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    
    private final JSqlClient sqlClient;
    private final LedgerMemberRepository ledgerMemberRepository;
    private final ExpenseRecordRepository expenseRecordRepository;
    private final ExpenseParticipantRepository expenseParticipantRepository;
    
    @Override
    public List<CategoryStatisticsResponse> getCategoryStatistics(Long ledgerId, LocalDate startDate, LocalDate endDate, Long userId) {
        validateMembership(ledgerId, userId);
        
        ExpenseRecordTable expense = Tables.EXPENSE_RECORD_TABLE;
        
        List<Tuple3<String, BigDecimal, Long>> results = sqlClient
            .createQuery(expense)
            .where(expense.ledgerId().eq(ledgerId))
            .where(expense.isDeleted().eq(false))
            .whereIf(startDate != null, () -> expense.expenseDate().ge(startDate))
            .whereIf(endDate != null, () -> expense.expenseDate().le(endDate))
            .groupBy(expense.categoryName())
            .select(
                expense.categoryName(),
                expense.amount().sum(),
                expense.count()
            )
            .execute();
        
        return results.stream()
            .map(tuple -> CategoryStatisticsResponse.builder()
                .categoryName(tuple.get_1())
                .totalAmount(tuple.get_2() != null ? tuple.get_2() : BigDecimal.ZERO)
                .expenseCount(tuple.get_3())
                .build())
            .sorted(Comparator.comparing(CategoryStatisticsResponse::getTotalAmount).reversed())
            .collect(Collectors.toList());
    }
    
    @Override
    public List<MemberStatisticsResponse> getMemberStatistics(Long ledgerId, LocalDate startDate, LocalDate endDate, Long userId) {
        validateMembership(ledgerId, userId);
        
        List<LedgerMember> members = ledgerMemberRepository
            .findByLedgerIdAndJoinStatusAndIsDeletedFalse(ledgerId, LedgerMember.JoinStatus.JOINED);
        
        ExpenseRecordTable expense = Tables.EXPENSE_RECORD_TABLE;
        
        Map<Long, Tuple2<BigDecimal, Long>> paymentStats = sqlClient
            .createQuery(expense)
            .where(expense.ledgerId().eq(ledgerId))
            .where(expense.isDeleted().eq(false))
            .whereIf(startDate != null, () -> expense.expenseDate().ge(startDate))
            .whereIf(endDate != null, () -> expense.expenseDate().le(endDate))
            .groupBy(expense.payerId())
            .select(
                expense.payerId(),
                expense.amount().sum(),
                expense.count()
            )
            .execute()
            .stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get_1(),
                tuple -> new Tuple2<>(tuple.get_2(), tuple.get_3())
            ));
        
        ExpenseParticipantTable participant = Tables.EXPENSE_PARTICIPANT_TABLE;
        
        Map<Long, BigDecimal> sharingStats = sqlClient
            .createQuery(participant)
            .where(participant.record().ledgerId().eq(ledgerId))
            .where(participant.record().isDeleted().eq(false))
            .whereIf(startDate != null, () -> participant.record().expenseDate().ge(startDate))
            .whereIf(endDate != null, () -> participant.record().expenseDate().le(endDate))
            .where(participant.isDeleted().eq(false))
            .groupBy(participant.userId())
            .select(
                participant.userId(),
                participant.amount().sum()
            )
            .execute()
            .stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get_1(),
                tuple -> tuple.get_2()
            ));
        
        return members.stream()
            .map(member -> {
                Long memberId = member.userId();
                BigDecimal totalPaid = paymentStats.containsKey(memberId) ? 
                    paymentStats.get(memberId).get_1() : BigDecimal.ZERO;
                Long expenseCount = paymentStats.containsKey(memberId) ? 
                    paymentStats.get(memberId).get_2() : 0L;
                BigDecimal totalShared = sharingStats.getOrDefault(memberId, BigDecimal.ZERO);
                BigDecimal balance = totalPaid.subtract(totalShared);
                
                return MemberStatisticsResponse.builder()
                    .userId(memberId)
                    .memberNickname(member.userNickname())
                    .totalPaid(totalPaid)
                    .totalShared(totalShared)
                    .balance(balance)
                    .expenseCount(expenseCount)
                    .build();
            })
            .sorted(Comparator.comparing(MemberStatisticsResponse::getTotalPaid).reversed())
            .collect(Collectors.toList());
    }
    
    @Override
    public List<TimeRangeStatisticsResponse> getTimeRangeStatistics(
            Long ledgerId, LocalDate startDate, LocalDate endDate,
            StatisticsRequest.TimeGranularity granularity, Long userId) {
        validateMembership(ledgerId, userId);
        
        StatisticsRequest.TimeGranularity finalGranularity = granularity != null ? 
            granularity : StatisticsRequest.TimeGranularity.MONTH;
        
        LocalDate finalStartDate = startDate;
        LocalDate finalEndDate = endDate;
        
        List<ExpenseRecord> expenses = expenseRecordRepository
            .findByLedgerIdAndIsDeletedFalse(ledgerId).stream()
            .filter(e -> {
                LocalDate expenseDate = e.expenseDate();
                if (finalStartDate != null && expenseDate.isBefore(finalStartDate)) return false;
                if (finalEndDate != null && expenseDate.isAfter(finalEndDate)) return false;
                return true;
            })
            .collect(Collectors.toList());
        
        if (finalGranularity == StatisticsRequest.TimeGranularity.CUSTOM) {
            BigDecimal total = expenses.stream()
                .map(ExpenseRecord::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            return Collections.singletonList(
                TimeRangeStatisticsResponse.builder()
                    .periodLabel("Custom Range")
                    .totalExpenses(total)
                    .expenseCount((long) expenses.size())
                    .build()
            );
        }
        
        Map<String, List<ExpenseRecord>> grouped = expenses.stream()
            .collect(Collectors.groupingBy(e -> formatPeriod(e.expenseDate(), finalGranularity)));
        
        return grouped.entrySet().stream()
            .map(entry -> {
                BigDecimal total = entry.getValue().stream()
                    .map(ExpenseRecord::amount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                return TimeRangeStatisticsResponse.builder()
                    .periodLabel(entry.getKey())
                    .totalExpenses(total)
                    .expenseCount((long) entry.getValue().size())
                    .build();
            })
            .sorted(Comparator.comparing(TimeRangeStatisticsResponse::getPeriodLabel))
            .collect(Collectors.toList());
    }
    
    @Override
    public OverallStatisticsResponse getOverallStatistics(
            Long ledgerId, LocalDate startDate, LocalDate endDate,
            StatisticsRequest.TimeGranularity granularity, Long userId) {
        validateMembership(ledgerId, userId);
        
        List<CategoryStatisticsResponse> categoryStats = getCategoryStatistics(ledgerId, startDate, endDate, userId);
        List<MemberStatisticsResponse> memberStats = getMemberStatistics(ledgerId, startDate, endDate, userId);
        List<TimeRangeStatisticsResponse> timeStats = getTimeRangeStatistics(ledgerId, startDate, endDate, granularity, userId);
        
        BigDecimal totalExpenses = categoryStats.stream()
            .map(CategoryStatisticsResponse::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Long totalExpenseCount = categoryStats.stream()
            .map(CategoryStatisticsResponse::getExpenseCount)
            .reduce(0L, Long::sum);
        
        return OverallStatisticsResponse.builder()
            .totalExpenses(totalExpenses)
            .totalExpenseCount(totalExpenseCount)
            .categoryStatistics(categoryStats)
            .memberStatistics(memberStats)
            .timeRangeStatistics(timeStats)
            .build();
    }
    
    private void validateMembership(Long ledgerId, Long userId) {
        LedgerMember member = ledgerMemberRepository
            .findByLedgerIdAndUserIdAndIsDeletedFalse(ledgerId, userId)
            .orElseThrow(() -> new RuntimeException("您不是该账本成员"));
        
        if (member.joinStatus() != LedgerMember.JoinStatus.JOINED) {
            throw new RuntimeException("只有已加入的成员才能查看统计");
        }
    }
    
    private String formatPeriod(LocalDate date, StatisticsRequest.TimeGranularity granularity) {
        if (granularity == StatisticsRequest.TimeGranularity.WEEK) {
            int year = date.get(IsoFields.WEEK_BASED_YEAR);
            int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            return String.format("%d-W%02d", year, week);
        } else {
            return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
    }
}
