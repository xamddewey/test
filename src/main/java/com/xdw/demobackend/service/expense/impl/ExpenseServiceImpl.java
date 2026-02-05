package com.xdw.demobackend.service.expense.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdw.demobackend.dto.expense.CreateExpenseRequest;
import com.xdw.demobackend.dto.expense.ExpenseResponse;
import com.xdw.demobackend.dto.expense.ParticipantRequest;
import com.xdw.demobackend.dto.expense.UpdateExpenseRequest;
import com.xdw.demobackend.entity.*;
import com.xdw.demobackend.repository.*;
import com.xdw.demobackend.service.expense.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {
    
    private final ExpenseRecordRepository expenseRecordRepository;
    private final ExpenseParticipantRepository expenseParticipantRepository;
    private final AccountLedgerRepository accountLedgerRepository;
    private final LedgerMemberRepository ledgerMemberRepository;
    private final LedgerCategoryRepository ledgerCategoryRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request, Long userId) {
        AccountLedger ledger = accountLedgerRepository.findByIdAndIsDeletedFalse(request.getLedgerId())
                .orElseThrow(() -> new RuntimeException("账本不存在，ID: " + request.getLedgerId()));
        
        LedgerMember creatorMember = ledgerMemberRepository
                .findByLedgerIdAndUserIdAndIsDeletedFalse(request.getLedgerId(), userId)
                .orElseThrow(() -> new RuntimeException("您不是该账本成员"));
        
        if (creatorMember.joinStatus() != LedgerMember.JoinStatus.JOINED) {
            throw new RuntimeException("只有已加入的成员才能创建记账");
        }
        
        ExpenseCategory category = expenseCategoryRepository.findByIdAndIsDeletedFalse(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("类别不存在，ID: " + request.getCategoryId()));
        
        if (!ledgerCategoryRepository.existsByLedgerIdAndCategoryIdAndIsDeletedFalse(
                request.getLedgerId(), request.getCategoryId())) {
            throw new RuntimeException("该类别未添加到账本中");
        }
        
        User payer = userRepository.findById(request.getPayerId())
                .orElseThrow(() -> new RuntimeException("付款人不存在，ID: " + request.getPayerId()));
        
        LedgerMember payerMember = ledgerMemberRepository
                .findByLedgerIdAndUserIdAndIsDeletedFalse(request.getLedgerId(), request.getPayerId())
                .orElseThrow(() -> new RuntimeException("付款人不是该账本成员"));
        
        if (payerMember.joinStatus() != LedgerMember.JoinStatus.JOINED) {
            throw new RuntimeException("付款人必须已加入账本");
        }
        
        for (ParticipantRequest participant : request.getParticipants()) {
            LedgerMember member = ledgerMemberRepository
                    .findByLedgerIdAndUserIdAndIsDeletedFalse(request.getLedgerId(), participant.getUserId())
                    .orElseThrow(() -> new RuntimeException("参与者不是账本成员，用户ID: " + participant.getUserId()));
            
            if (member.joinStatus() != LedgerMember.JoinStatus.JOINED) {
                throw new RuntimeException("参与者必须已加入账本，用户ID: " + participant.getUserId());
            }
        }
        
        BigDecimal totalParticipantAmount = request.getParticipants().stream()
                .map(ParticipantRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalParticipantAmount.compareTo(request.getAmount()) != 0) {
            throw new RuntimeException("参与者分摊金额之和必须等于总金额");
        }
        
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("创建者不存在，ID: " + userId));
        
        LocalDateTime now = LocalDateTime.now();
        
        int participantCount = request.getParticipants().size();
        BigDecimal avgAmount = request.getAmount().divide(
                BigDecimal.valueOf(participantCount), 2, RoundingMode.HALF_UP);
        
        List<Map<String, Object>> participantsInfo = new ArrayList<>();
        for (ParticipantRequest p : request.getParticipants()) {
            User participantUser = userRepository.findById(p.getUserId())
                    .orElseThrow(() -> new RuntimeException("参与者不存在，ID: " + p.getUserId()));
            Map<String, Object> info = new HashMap<>();
            info.put("userId", p.getUserId());
            info.put("nickname", participantUser.nickname() != null ? participantUser.nickname() : participantUser.username());
            info.put("amount", p.getAmount());
            participantsInfo.add(info);
        }
        
        String participantsInfoJson;
        try {
            participantsInfoJson = objectMapper.writeValueAsString(participantsInfo);
        } catch (Exception e) {
            log.error("Failed to serialize participants info", e);
            participantsInfoJson = "[]";
        }
        final String finalParticipantsInfoJson = participantsInfoJson;
        
        ExpenseRecord expense = expenseRecordRepository.insert(
                ExpenseRecordDraft.$.produce(draft -> {
                    draft.setLedger(AccountLedgerDraft.$.produce(l -> l.setId(request.getLedgerId())));
                    draft.setCategory(ExpenseCategoryDraft.$.produce(c -> c.setId(request.getCategoryId())));
                    draft.setPayer(UserDraft.$.produce(u -> u.setId(request.getPayerId())));
                    draft.setCreatedBy(UserDraft.$.produce(u -> u.setId(userId)));
                    draft.setAmount(request.getAmount());
                    draft.setDescription(request.getDescription());
                    draft.setExpenseDate(request.getExpenseDate());
                    draft.setLedgerName(ledger.ledgerName());
                    draft.setCategoryName(category.categoryName());
                    draft.setPayerNickname(payer.nickname() != null ? payer.nickname() : payer.username());
                    draft.setCreatorNickname(creator.nickname() != null ? creator.nickname() : creator.username());
                    draft.setParticipantCount(participantCount);
                    draft.setAvgAmount(avgAmount);
                    draft.setParticipantsInfo(finalParticipantsInfoJson);
                    draft.setHasSettlement(false);
                    draft.setCreatedAt(now);
                    draft.setUpdatedAt(now);
                    draft.setIsDeleted(false);
                })
        );
        
        for (ParticipantRequest participantReq : request.getParticipants()) {
            User participantUser = userRepository.findById(participantReq.getUserId())
                    .orElseThrow(() -> new RuntimeException("参与者不存在"));
            
            expenseParticipantRepository.insert(
                    ExpenseParticipantDraft.$.produce(draft -> {
                        draft.setRecord(ExpenseRecordDraft.$.produce(r -> r.setId(expense.id())));
                        draft.setUser(UserDraft.$.produce(u -> u.setId(participantReq.getUserId())));
                        draft.setAmount(participantReq.getAmount());
                        draft.setUserNickname(participantUser.nickname() != null ? 
                                participantUser.nickname() : participantUser.username());
                        draft.setExpenseAmount(request.getAmount());
                        draft.setExpenseDate(request.getExpenseDate());
                        draft.setLedgerId(request.getLedgerId());
                        draft.setCategoryName(category.categoryName());
                        draft.setIsDeleted(false);
                    })
            );
        }
        
        AccountLedger updatedLedger = AccountLedgerDraft.$.produce(ledger, draft -> {
            draft.setTotalExpenses((ledger.totalExpenses() != null ? ledger.totalExpenses() : BigDecimal.ZERO)
                    .add(request.getAmount()));
            draft.setRecordCount((ledger.recordCount() != null ? ledger.recordCount() : 0) + 1);
            draft.setLastExpenseDate(request.getExpenseDate());
            draft.setLastActivityAt(now);
            draft.setUpdatedAt(now);
        });
        accountLedgerRepository.save(updatedLedger);
        
        BigDecimal newPayerTotalPaid = (payerMember.totalPaid() != null ? 
                payerMember.totalPaid() : BigDecimal.ZERO).add(request.getAmount());
        BigDecimal payerTotalShared = payerMember.totalShared() != null ? 
                payerMember.totalShared() : BigDecimal.ZERO;
        
        LedgerMember updatedPayerMember = LedgerMemberDraft.$.produce(payerMember, draft -> {
            draft.setTotalPaid(newPayerTotalPaid);
            draft.setRecordCount((payerMember.recordCount() != null ? payerMember.recordCount() : 0) + 1);
            draft.setLastActivityAt(now);
            draft.setBalance(newPayerTotalPaid.subtract(payerTotalShared));
        });
        ledgerMemberRepository.save(updatedPayerMember);
        
        for (ParticipantRequest participantReq : request.getParticipants()) {
            LedgerMember participantMember = ledgerMemberRepository
                    .findByLedgerIdAndUserIdAndIsDeletedFalse(request.getLedgerId(), participantReq.getUserId())
                    .orElseThrow(() -> new RuntimeException("参与者成员记录不存在"));
            
            BigDecimal newParticipantTotalShared = (participantMember.totalShared() != null ? 
                    participantMember.totalShared() : BigDecimal.ZERO).add(participantReq.getAmount());
            BigDecimal participantTotalPaid = participantMember.totalPaid() != null ? 
                    participantMember.totalPaid() : BigDecimal.ZERO;
            
            LedgerMember updatedParticipant = LedgerMemberDraft.$.produce(participantMember, draft -> {
                draft.setTotalShared(newParticipantTotalShared);
                draft.setLastActivityAt(now);
                draft.setBalance(participantTotalPaid.subtract(newParticipantTotalShared));
            });
            ledgerMemberRepository.save(updatedParticipant);
        }
        
        LedgerCategory ledgerCategory = ledgerCategoryRepository
                .findByLedgerIdAndCategoryIdAndIsDeletedFalse(request.getLedgerId(), request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("账本类别关联不存在"));
        
        LedgerCategory updatedLedgerCategory = LedgerCategoryDraft.$.produce(ledgerCategory, draft -> {
            draft.setUsageCount((ledgerCategory.usageCount() != null ? ledgerCategory.usageCount() : 0) + 1);
            draft.setUpdatedAt(now);
        });
        ledgerCategoryRepository.save(updatedLedgerCategory);
        
        ExpenseCategory updatedCategory = ExpenseCategoryDraft.$.produce(category, draft -> {
            draft.setUsageCount((category.usageCount() != null ? category.usageCount() : 0) + 1);
            draft.setUpdatedAt(now);
        });
        expenseCategoryRepository.save(updatedCategory);
        
        log.info("Expense created successfully, ID: {}", expense.id());
        
        ExpenseRecord reloaded = expenseRecordRepository.findByIdAndIsDeletedFalse(expense.id())
                .orElseThrow(() -> new RuntimeException("记账条目创建后未找到"));
        
        return ExpenseResponse.fromEntity(reloaded);
    }
    
    @Override
    public ExpenseResponse getExpenseById(Long expenseId, Long userId) {
        ExpenseRecord expense = expenseRecordRepository.findByIdAndIsDeletedFalse(expenseId)
                .orElseThrow(() -> new RuntimeException("记账条目不存在，ID: " + expenseId));
        
        if (!ledgerMemberRepository.existsByLedgerIdAndUserIdAndIsDeletedFalse(expense.ledgerId(), userId)) {
            throw new RuntimeException("无权访问该记账条目");
        }
        
        return ExpenseResponse.fromEntity(expense);
    }
    
    @Override
    public List<ExpenseResponse> getLedgerExpenses(Long ledgerId, Long userId) {
        if (!accountLedgerRepository.existsByIdAndIsDeletedFalse(ledgerId)) {
            throw new RuntimeException("账本不存在，ID: " + ledgerId);
        }
        
        if (!ledgerMemberRepository.existsByLedgerIdAndUserIdAndIsDeletedFalse(ledgerId, userId)) {
            throw new RuntimeException("无权访问该账本");
        }
        
        List<ExpenseRecord> expenses = expenseRecordRepository
                .findByLedgerIdAndIsDeletedFalseOrderByExpenseDateDesc(ledgerId);
        
        return expenses.stream()
                .map(ExpenseResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<ExpenseResponse> getLedgerExpensesPaged(Long ledgerId, Long userId, Pageable pageable) {
        if (!accountLedgerRepository.existsByIdAndIsDeletedFalse(ledgerId)) {
            throw new RuntimeException("账本不存在，ID: " + ledgerId);
        }
        
        if (!ledgerMemberRepository.existsByLedgerIdAndUserIdAndIsDeletedFalse(ledgerId, userId)) {
            throw new RuntimeException("无权访问该账本");
        }
        
        List<ExpenseRecord> allExpenses = expenseRecordRepository
                .findByLedgerIdAndIsDeletedFalseOrderByExpenseDateDesc(ledgerId);
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allExpenses.size());
        
        List<ExpenseResponse> pageContent = allExpenses.subList(start, end).stream()
                .map(ExpenseResponse::fromEntity)
                .collect(Collectors.toList());
        
        return new PageImpl<>(pageContent, pageable, allExpenses.size());
    }
    
    @Override
    @Transactional
    public ExpenseResponse updateExpense(Long expenseId, UpdateExpenseRequest request, Long userId) {
        ExpenseRecord expense = expenseRecordRepository.findByIdAndIsDeletedFalse(expenseId)
                .orElseThrow(() -> new RuntimeException("记账条目不存在，ID: " + expenseId));
        
        LedgerMember member = ledgerMemberRepository
                .findByLedgerIdAndUserIdAndIsDeletedFalse(expense.ledgerId(), userId)
                .orElseThrow(() -> new RuntimeException("无权修改该记账条目"));
        
        if (member.joinStatus() != LedgerMember.JoinStatus.JOINED) {
            throw new RuntimeException("只有已加入的成员才能修改记账");
        }
        
        BigDecimal oldAmount = expense.amount();
        Long oldPayerId = expense.payerId();
        List<ExpenseParticipant> oldParticipants = expenseParticipantRepository
                .findByRecordIdAndIsDeletedFalse(expenseId);
        
        LocalDateTime now = LocalDateTime.now();
        
        ExpenseRecord updated = ExpenseRecordDraft.$.produce(expense, draft -> {
            if (request.getCategoryId() != null) {
                ExpenseCategory newCategory = expenseCategoryRepository.findByIdAndIsDeletedFalse(request.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("类别不存在"));
                draft.setCategory(ExpenseCategoryDraft.$.produce(c -> c.setId(request.getCategoryId())));
                draft.setCategoryName(newCategory.categoryName());
            }
            
            if (request.getPayerId() != null) {
                User newPayer = userRepository.findById(request.getPayerId())
                        .orElseThrow(() -> new RuntimeException("付款人不存在"));
                draft.setPayer(UserDraft.$.produce(u -> u.setId(request.getPayerId())));
                draft.setPayerNickname(newPayer.nickname() != null ? newPayer.nickname() : newPayer.username());
            }
            
            if (request.getAmount() != null) {
                draft.setAmount(request.getAmount());
            }
            
            if (request.getDescription() != null) {
                draft.setDescription(request.getDescription());
            }
            
            if (request.getExpenseDate() != null) {
                draft.setExpenseDate(request.getExpenseDate());
            }
            
            draft.setUpdatedAt(now);
        });
        
        if (request.getParticipants() != null && !request.getParticipants().isEmpty()) {
            BigDecimal newAmount = request.getAmount() != null ? request.getAmount() : expense.amount();
            BigDecimal totalParticipantAmount = request.getParticipants().stream()
                    .map(ParticipantRequest::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (totalParticipantAmount.compareTo(newAmount) != 0) {
                throw new RuntimeException("参与者分摊金额之和必须等于总金额");
            }
            
            int participantCount = request.getParticipants().size();
            BigDecimal avgAmount = newAmount.divide(
                    BigDecimal.valueOf(participantCount), 2, RoundingMode.HALF_UP);
            
            updated = ExpenseRecordDraft.$.produce(updated, draft -> {
                draft.setParticipantCount(participantCount);
                draft.setAvgAmount(avgAmount);
            });
        }
        
        ExpenseRecord saved = expenseRecordRepository.save(updated);
        
        if (request.getParticipants() != null && !request.getParticipants().isEmpty()) {
            for (ExpenseParticipant oldParticipant : oldParticipants) {
                ExpenseParticipant deleted = ExpenseParticipantDraft.$.produce(oldParticipant, draft -> {
                    draft.setIsDeleted(true);
                    draft.setDeletedAt(now);
                });
                expenseParticipantRepository.save(deleted);
            }
            
            ExpenseCategory category = request.getCategoryId() != null ?
                    expenseCategoryRepository.findByIdAndIsDeletedFalse(request.getCategoryId())
                            .orElseThrow(() -> new RuntimeException("类别不存在")) :
                    expenseCategoryRepository.findByIdAndIsDeletedFalse(expense.categoryId())
                            .orElseThrow(() -> new RuntimeException("原类别不存在"));
            
            for (ParticipantRequest participantReq : request.getParticipants()) {
                User participantUser = userRepository.findById(participantReq.getUserId())
                        .orElseThrow(() -> new RuntimeException("参与者不存在"));
                
                expenseParticipantRepository.insert(
                        ExpenseParticipantDraft.$.produce(draft -> {
                            draft.setRecord(ExpenseRecordDraft.$.produce(r -> r.setId(expenseId)));
                            draft.setUser(UserDraft.$.produce(u -> u.setId(participantReq.getUserId())));
                            draft.setAmount(participantReq.getAmount());
                            draft.setUserNickname(participantUser.nickname() != null ? 
                                    participantUser.nickname() : participantUser.username());
                            draft.setExpenseAmount(saved.amount());
                            draft.setExpenseDate(saved.expenseDate());
                            draft.setLedgerId(expense.ledgerId());
                            draft.setCategoryName(category.categoryName());
                            draft.setIsDeleted(false);
                        })
                );
            }
        }
        
        if (request.getAmount() != null) {
            AccountLedger ledger = accountLedgerRepository.findByIdAndIsDeletedFalse(expense.ledgerId())
                    .orElseThrow(() -> new RuntimeException("账本不存在"));
            
            BigDecimal amountDiff = request.getAmount().subtract(oldAmount);
            AccountLedger updatedLedger = AccountLedgerDraft.$.produce(ledger, draft -> {
                draft.setTotalExpenses((ledger.totalExpenses() != null ? 
                        ledger.totalExpenses() : BigDecimal.ZERO).add(amountDiff));
                draft.setLastActivityAt(now);
                draft.setUpdatedAt(now);
            });
            accountLedgerRepository.save(updatedLedger);
        }
        
        log.info("Expense updated successfully, ID: {}", expenseId);
        
        ExpenseRecord reloaded = expenseRecordRepository.findByIdAndIsDeletedFalse(expenseId)
                .orElseThrow(() -> new RuntimeException("记账条目更新后未找到"));
        
        return ExpenseResponse.fromEntity(reloaded);
    }
    
    @Override
    @Transactional
    public void deleteExpense(Long expenseId, Long userId) {
        ExpenseRecord expense = expenseRecordRepository.findByIdAndIsDeletedFalse(expenseId)
                .orElseThrow(() -> new RuntimeException("记账条目不存在，ID: " + expenseId));
        
        LedgerMember member = ledgerMemberRepository
                .findByLedgerIdAndUserIdAndIsDeletedFalse(expense.ledgerId(), userId)
                .orElseThrow(() -> new RuntimeException("无权删除该记账条目"));
        
        if (member.joinStatus() != LedgerMember.JoinStatus.JOINED) {
            throw new RuntimeException("只有已加入的成员才能删除记账");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        ExpenseRecord deleted = ExpenseRecordDraft.$.produce(expense, draft -> {
            draft.setIsDeleted(true);
            draft.setDeletedAt(now);
        });
        expenseRecordRepository.save(deleted);
        
        List<ExpenseParticipant> participants = expenseParticipantRepository
                .findByRecordIdAndIsDeletedFalse(expenseId);
        
        for (ExpenseParticipant participant : participants) {
            ExpenseParticipant deletedParticipant = ExpenseParticipantDraft.$.produce(participant, draft -> {
                draft.setIsDeleted(true);
                draft.setDeletedAt(now);
            });
            expenseParticipantRepository.save(deletedParticipant);
        }
        
        AccountLedger ledger = accountLedgerRepository.findByIdAndIsDeletedFalse(expense.ledgerId())
                .orElseThrow(() -> new RuntimeException("账本不存在"));
        
        AccountLedger updatedLedger = AccountLedgerDraft.$.produce(ledger, draft -> {
            draft.setTotalExpenses((ledger.totalExpenses() != null ? 
                    ledger.totalExpenses() : BigDecimal.ZERO).subtract(expense.amount()));
            draft.setRecordCount((ledger.recordCount() != null ? ledger.recordCount() : 1) - 1);
            draft.setLastActivityAt(now);
            draft.setUpdatedAt(now);
        });
        accountLedgerRepository.save(updatedLedger);
        
        LedgerMember payerMember = ledgerMemberRepository
                .findByLedgerIdAndUserIdAndIsDeletedFalse(expense.ledgerId(), expense.payerId())
                .orElseThrow(() -> new RuntimeException("付款人成员记录不存在"));
        
        BigDecimal newPayerTotalPaid = (payerMember.totalPaid() != null ? 
                payerMember.totalPaid() : BigDecimal.ZERO).subtract(expense.amount());
        BigDecimal payerTotalShared = payerMember.totalShared() != null ? 
                payerMember.totalShared() : BigDecimal.ZERO;
        
        LedgerMember updatedPayerMember = LedgerMemberDraft.$.produce(payerMember, draft -> {
            draft.setTotalPaid(newPayerTotalPaid);
            draft.setRecordCount((payerMember.recordCount() != null ? payerMember.recordCount() : 1) - 1);
            draft.setBalance(newPayerTotalPaid.subtract(payerTotalShared));
        });
        ledgerMemberRepository.save(updatedPayerMember);
        
        for (ExpenseParticipant participant : participants) {
            LedgerMember participantMember = ledgerMemberRepository
                    .findByLedgerIdAndUserIdAndIsDeletedFalse(expense.ledgerId(), participant.userId())
                    .orElseThrow(() -> new RuntimeException("参与者成员记录不存在"));
            
            BigDecimal newParticipantTotalShared = (participantMember.totalShared() != null ? 
                    participantMember.totalShared() : BigDecimal.ZERO).subtract(participant.amount());
            BigDecimal participantTotalPaid = participantMember.totalPaid() != null ? 
                    participantMember.totalPaid() : BigDecimal.ZERO;
            
            LedgerMember updatedParticipant = LedgerMemberDraft.$.produce(participantMember, draft -> {
                draft.setTotalShared(newParticipantTotalShared);
                draft.setBalance(participantTotalPaid.subtract(newParticipantTotalShared));
            });
            ledgerMemberRepository.save(updatedParticipant);
        }
        
        log.info("Expense deleted successfully, ID: {}", expenseId);
    }
}
