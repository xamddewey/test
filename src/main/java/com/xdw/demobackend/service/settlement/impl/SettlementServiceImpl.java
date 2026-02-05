package com.xdw.demobackend.service.settlement.impl;

import com.xdw.demobackend.dto.settlement.CreateSettlementRequest;
import com.xdw.demobackend.dto.settlement.SettlementCalculationResponse;
import com.xdw.demobackend.dto.settlement.SettlementResponse;
import com.xdw.demobackend.dto.settlement.TransferDTO;
import com.xdw.demobackend.entity.*;
import com.xdw.demobackend.repository.*;
import com.xdw.demobackend.service.settlement.SettlementAlgorithm;
import com.xdw.demobackend.service.settlement.SettlementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class SettlementServiceImpl implements SettlementService {

    private static final Logger log = LoggerFactory.getLogger(SettlementServiceImpl.class);

    private final SettlementRepository settlementRepository;
    private final LedgerMemberRepository ledgerMemberRepository;
    private final AccountLedgerRepository accountLedgerRepository;
    private final UserRepository userRepository;

    public SettlementServiceImpl(
            SettlementRepository settlementRepository,
            LedgerMemberRepository ledgerMemberRepository,
            AccountLedgerRepository accountLedgerRepository,
            UserRepository userRepository
    ) {
        this.settlementRepository = settlementRepository;
        this.ledgerMemberRepository = ledgerMemberRepository;
        this.accountLedgerRepository = accountLedgerRepository;
        this.userRepository = userRepository;
    }

    @Override
    public SettlementCalculationResponse calculateSettlement(Long ledgerId, Long userId) {
        AccountLedger ledger = accountLedgerRepository.findByIdAndIsDeletedFalse(ledgerId)
                .orElseThrow(() -> new RuntimeException("账本不存在，ID: " + ledgerId));

        if (!ledgerMemberRepository.existsByLedgerIdAndUserIdAndIsDeletedFalse(ledgerId, userId)) {
            throw new RuntimeException("无权访问该账本");
        }

        List<LedgerMember> members = ledgerMemberRepository
                .findByLedgerIdAndJoinStatusAndIsDeletedFalse(ledgerId, LedgerMember.JoinStatus.JOINED);

        Map<Long, BigDecimal> balances = new HashMap<>();
        for (LedgerMember member : members) {
            BigDecimal balance = member.balance() != null ? member.balance() : BigDecimal.ZERO;
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                balances.put(member.userId(), balance);
            }
        }

        List<TransferDTO> transfers = SettlementAlgorithm.calculateMinimumTransfers(balances);

        Map<Long, LedgerMember> memberMap = members.stream()
                .collect(Collectors.toMap(LedgerMember::userId, m -> m));

        List<TransferDTO> enrichedTransfers = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (TransferDTO transfer : transfers) {
            LedgerMember payer = memberMap.get(transfer.payerId());
            LedgerMember receiver = memberMap.get(transfer.receiverId());
            BigDecimal amount = transfer.amount();
            totalAmount = totalAmount.add(amount != null ? amount : BigDecimal.ZERO);

            enrichedTransfers.add(new TransferDTO(
                    transfer.payerId(),
                    payer != null ? payer.userNickname() : null,
                    transfer.receiverId(),
                    receiver != null ? receiver.userNickname() : null,
                    amount
            ));
        }

        return new SettlementCalculationResponse(
                ledgerId,
                ledger.ledgerName(),
                enrichedTransfers,
                enrichedTransfers.size(),
                totalAmount
        );
    }

    @Override
    @Transactional
    public SettlementResponse createSettlement(CreateSettlementRequest request, Long userId) {
        AccountLedger ledger = accountLedgerRepository.findByIdAndIsDeletedFalse(request.ledgerId())
                .orElseThrow(() -> new RuntimeException("账本不存在，ID: " + request.ledgerId()));

        if (!ledgerMemberRepository.existsByLedgerIdAndUserIdAndIsDeletedFalse(request.ledgerId(), userId)) {
            throw new RuntimeException("无权创建结算");
        }

        if (request.payerId().equals(request.receiverId())) {
            throw new RuntimeException("付款人与收款人不能相同");
        }

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("金额必须大于0");
        }

        LedgerMember payerMember = ledgerMemberRepository
                .findByLedgerIdAndUserIdAndIsDeletedFalse(request.ledgerId(), request.payerId())
                .orElseThrow(() -> new RuntimeException("付款人不是该账本成员"));

        LedgerMember receiverMember = ledgerMemberRepository
                .findByLedgerIdAndUserIdAndIsDeletedFalse(request.ledgerId(), request.receiverId())
                .orElseThrow(() -> new RuntimeException("收款人不是该账本成员"));

        if (payerMember.joinStatus() != LedgerMember.JoinStatus.JOINED
                || receiverMember.joinStatus() != LedgerMember.JoinStatus.JOINED) {
            throw new RuntimeException("付款人和收款人必须已加入账本");
        }

        User payer = userRepository.findById(request.payerId())
                .orElseThrow(() -> new RuntimeException("付款人不存在，ID: " + request.payerId()));

        User receiver = userRepository.findById(request.receiverId())
                .orElseThrow(() -> new RuntimeException("收款人不存在，ID: " + request.receiverId()));

        BigDecimal amount = request.amount().setScale(2, RoundingMode.DOWN);
        LocalDateTime now = LocalDateTime.now();

        Settlement settlement = settlementRepository.insert(
                SettlementDraft.$.produce(draft -> {
                    draft.setLedger(AccountLedgerDraft.$.produce(l -> l.setId(request.ledgerId())));
                    draft.setPayer(UserDraft.$.produce(u -> u.setId(request.payerId())));
                    draft.setReceiver(UserDraft.$.produce(u -> u.setId(request.receiverId())));
                    draft.setAmount(amount);
                    draft.setStatus(Settlement.SettlementStatus.PENDING);
                    draft.setDescription(request.description());
                    draft.setLedgerName(ledger.ledgerName());
                    draft.setPayerNickname(payer.nickname() != null ? payer.nickname() : payer.username());
                    draft.setReceiverNickname(receiver.nickname() != null ? receiver.nickname() : receiver.username());
                    draft.setCreatedAt(now);
                    draft.setUpdatedAt(now);
                    draft.setIsDeleted(false);
                })
        );

        AccountLedger updatedLedger = AccountLedgerDraft.$.produce(ledger, draft -> {
            draft.setLastActivityAt(now);
            draft.setUpdatedAt(now);
        });
        accountLedgerRepository.save(updatedLedger);

        log.info("Settlement created, ID: {}", settlement.id());

        Settlement reloaded = settlementRepository.findByIdAndIsDeletedFalse(settlement.id())
                .orElseThrow(() -> new RuntimeException("结算创建后未找到"));

        return SettlementResponse.fromEntity(reloaded);
    }

    @Override
    @Transactional
    public SettlementResponse completeSettlement(Long settlementId, Long userId) {
        Settlement settlement = settlementRepository.findByIdAndIsDeletedFalse(settlementId)
                .orElseThrow(() -> new RuntimeException("结算不存在，ID: " + settlementId));

        if (settlement.payerId() != userId && settlement.receiverId() != userId) {
            throw new RuntimeException("只有付款人或收款人可以完成结算");
        }

        if (settlement.status() != Settlement.SettlementStatus.PENDING) {
            throw new RuntimeException("结算状态不是待完成");
        }

        LocalDateTime now = LocalDateTime.now();

        Settlement completed = SettlementDraft.$.produce(settlement, draft -> {
            draft.setStatus(Settlement.SettlementStatus.COMPLETED);
            draft.setUpdatedAt(now);
        });
        settlementRepository.save(completed);

        LedgerMember payerMember = ledgerMemberRepository
                .findByLedgerIdAndUserIdAndIsDeletedFalse(settlement.ledgerId(), settlement.payerId())
                .orElseThrow(() -> new RuntimeException("付款人成员记录不存在"));

        LedgerMember receiverMember = ledgerMemberRepository
                .findByLedgerIdAndUserIdAndIsDeletedFalse(settlement.ledgerId(), settlement.receiverId())
                .orElseThrow(() -> new RuntimeException("收款人成员记录不存在"));

        BigDecimal amount = settlement.amount();

        LedgerMember updatedPayer = LedgerMemberDraft.$.produce(payerMember, draft -> {
            BigDecimal balance = payerMember.balance() != null ? payerMember.balance() : BigDecimal.ZERO;
            draft.setBalance(balance.add(amount));
            draft.setLastActivityAt(now);
        });
        ledgerMemberRepository.save(updatedPayer);

        LedgerMember updatedReceiver = LedgerMemberDraft.$.produce(receiverMember, draft -> {
            BigDecimal balance = receiverMember.balance() != null ? receiverMember.balance() : BigDecimal.ZERO;
            draft.setBalance(balance.subtract(amount));
            draft.setLastActivityAt(now);
        });
        ledgerMemberRepository.save(updatedReceiver);

        AccountLedger ledger = accountLedgerRepository.findByIdAndIsDeletedFalse(settlement.ledgerId())
                .orElseThrow(() -> new RuntimeException("账本不存在"));

        AccountLedger updatedLedger = AccountLedgerDraft.$.produce(ledger, draft -> {
            draft.setLastActivityAt(now);
            draft.setUpdatedAt(now);
        });
        accountLedgerRepository.save(updatedLedger);

        Settlement reloaded = settlementRepository.findByIdAndIsDeletedFalse(settlementId)
                .orElseThrow(() -> new RuntimeException("结算更新后未找到"));

        return SettlementResponse.fromEntity(reloaded);
    }

    @Override
    public List<SettlementResponse> getLedgerSettlements(Long ledgerId, String status, Long userId) {
        if (!accountLedgerRepository.existsByIdAndIsDeletedFalse(ledgerId)) {
            throw new RuntimeException("账本不存在，ID: " + ledgerId);
        }

        if (!ledgerMemberRepository.existsByLedgerIdAndUserIdAndIsDeletedFalse(ledgerId, userId)) {
            throw new RuntimeException("无权查看该账本结算");
        }

        List<Settlement> settlements;
        if (status != null) {
            settlements = settlementRepository.findByLedgerIdAndStatusAndIsDeletedFalse(
                    ledgerId, Settlement.SettlementStatus.valueOf(status));
        } else {
            settlements = settlementRepository.findByLedgerIdAndIsDeletedFalse(ledgerId);
        }

        return settlements.stream()
                .map(SettlementResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<SettlementResponse> getMySettlements(Long userId, String status) {
        List<Settlement> settlements = new ArrayList<>();
        if (status != null) {
            Settlement.SettlementStatus settlementStatus = Settlement.SettlementStatus.valueOf(status);
            settlements.addAll(settlementRepository.findByPayerIdAndStatusAndIsDeletedFalse(userId, settlementStatus));
            settlements.addAll(settlementRepository.findByReceiverIdAndStatusAndIsDeletedFalse(userId, settlementStatus));
        } else {
            settlements.addAll(settlementRepository.findByPayerIdAndIsDeletedFalse(userId));
            settlements.addAll(settlementRepository.findByReceiverIdAndIsDeletedFalse(userId));
        }

        return settlements.stream()
                .map(SettlementResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
