package com.xdw.demobackend.service.ledger.impl;

import com.xdw.demobackend.dto.ledger.CreateLedgerRequest;
import com.xdw.demobackend.dto.ledger.LedgerResponse;
import com.xdw.demobackend.dto.ledger.MemberResponse;
import com.xdw.demobackend.dto.ledger.UpdateLedgerRequest;
import com.xdw.demobackend.entity.*;
import com.xdw.demobackend.repository.AccountLedgerRepository;
import com.xdw.demobackend.repository.LedgerMemberRepository;
import com.xdw.demobackend.repository.UserRepository;
import com.xdw.demobackend.service.ledger.LedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerServiceImpl implements LedgerService {
    
    private final AccountLedgerRepository accountLedgerRepository;
    private final LedgerMemberRepository ledgerMemberRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public LedgerResponse createLedger(CreateLedgerRequest request, Long userId) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));
        
        LocalDateTime now = LocalDateTime.now();
        
        AccountLedger ledger = accountLedgerRepository.insert(
                AccountLedgerDraft.$.produce(draft -> {
                    draft.setLedgerName(request.getLedgerName());
                    draft.setDescription(request.getDescription());
                    draft.setCreator(UserDraft.$.produce(u -> u.setId(userId)));
                    draft.setCreatorNickname(creator.nickname() != null ? creator.nickname() : creator.username());
                    draft.setMemberCount(1);
                    draft.setInvitedCount(0);
                    draft.setTotalExpenses(BigDecimal.ZERO);
                    draft.setRecordCount(0);
                    draft.setCreatedAt(now);
                    draft.setUpdatedAt(now);
                    draft.setLastActivityAt(now);
                    draft.setIsDeleted(false);
                })
        );
        
        ledgerMemberRepository.insert(
                LedgerMemberDraft.$.produce(draft -> {
                    draft.setLedger(AccountLedgerDraft.$.produce(l -> l.setId(ledger.id())));
                    draft.setUser(UserDraft.$.produce(u -> u.setId(userId)));
                    draft.setLedgerName(ledger.ledgerName());
                    draft.setUserNickname(creator.nickname() != null ? creator.nickname() : creator.username());
                    draft.setJoinStatus(LedgerMember.JoinStatus.JOINED);
                    draft.setTotalPaid(BigDecimal.ZERO);
                    draft.setTotalShared(BigDecimal.ZERO);
                    draft.setBalance(BigDecimal.ZERO);
                    draft.setRecordCount(0);
                    draft.setJoinedAt(now);
                    draft.setLastActivityAt(now);
                    draft.setIsDeleted(false);
                })
        );
        
        log.info("Ledger created successfully, ID: {}", ledger.id());
        return LedgerResponse.fromEntity(ledger);
    }
    
    @Override
    public LedgerResponse getLedger(Long ledgerId, Long userId) {
        log.info("Getting ledger: {} for user: {}", ledgerId, userId);
        
        AccountLedger ledger = accountLedgerRepository.findByIdAndIsDeletedFalse(ledgerId)
                .orElseThrow(() -> new RuntimeException("账本不存在，ID: " + ledgerId));
        
        if (!ledgerMemberRepository.existsByLedgerIdAndUserIdAndIsDeletedFalse(ledgerId, userId)) {
            throw new RuntimeException("无权访问该账本");
        }
        
        return LedgerResponse.fromEntity(ledger);
    }
    
    @Override
    public List<LedgerResponse> getUserLedgers(Long userId) {
        log.info("Getting ledgers for user: {}", userId);
        
        List<LedgerMember> members = ledgerMemberRepository
                .findByUserIdAndJoinStatusAndIsDeletedFalse(userId, LedgerMember.JoinStatus.JOINED);
        
        return members.stream()
                .map(member -> {
                    AccountLedger ledger = accountLedgerRepository
                            .findByIdAndIsDeletedFalse(member.ledgerId())
                            .orElse(null);
                    return ledger != null ? LedgerResponse.fromEntity(ledger) : null;
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public LedgerResponse updateLedger(Long ledgerId, UpdateLedgerRequest request, Long userId) {
        log.info("Updating ledger: {} by user: {}", ledgerId, userId);
        
        AccountLedger ledger = accountLedgerRepository.findByIdAndIsDeletedFalse(ledgerId)
                .orElseThrow(() -> new RuntimeException("账本不存在，ID: " + ledgerId));
        
        if (ledger.creatorId() != userId) {
            throw new RuntimeException("只有创建者可以修改账本");
        }
        
        boolean ledgerNameChanged = request.getLedgerName() != null && 
                !request.getLedgerName().equals(ledger.ledgerName());
        
        AccountLedger updated = AccountLedgerDraft.$.produce(ledger, draft -> {
            if (request.getLedgerName() != null) {
                draft.setLedgerName(request.getLedgerName());
            }
            if (request.getDescription() != null) {
                draft.setDescription(request.getDescription());
            }
            draft.setUpdatedAt(LocalDateTime.now());
            draft.setLastActivityAt(LocalDateTime.now());
        });
        
        AccountLedger saved = accountLedgerRepository.save(updated);
        
        if (ledgerNameChanged) {
            List<LedgerMember> members = ledgerMemberRepository.findByLedgerIdAndIsDeletedFalse(ledgerId);
            for (LedgerMember member : members) {
                LedgerMember updatedMember = LedgerMemberDraft.$.produce(member, draft -> {
                    draft.setLedgerName(request.getLedgerName());
                });
                ledgerMemberRepository.save(updatedMember);
            }
        }
        
        log.info("Ledger updated successfully, ID: {}", ledgerId);
        return LedgerResponse.fromEntity(saved);
    }
    
    @Override
    @Transactional
    public void deleteLedger(Long ledgerId, Long userId) {
        log.info("Deleting ledger: {} by user: {}", ledgerId, userId);
        
        AccountLedger ledger = accountLedgerRepository.findByIdAndIsDeletedFalse(ledgerId)
                .orElseThrow(() -> new RuntimeException("账本不存在，ID: " + ledgerId));
        
        if (ledger.creatorId() != userId) {
            throw new RuntimeException("只有创建者可以删除账本");
        }
        
        LocalDateTime now = LocalDateTime.now();
        AccountLedger deleted = AccountLedgerDraft.$.produce(ledger, draft -> {
            draft.setIsDeleted(true);
            draft.setDeletedAt(now);
        });
        
        accountLedgerRepository.save(deleted);
        log.info("Ledger soft deleted successfully, ID: {}", ledgerId);
    }
    
    @Override
    public List<MemberResponse> getLedgerMembers(Long ledgerId, Long userId) {
        log.info("Getting members of ledger: {} for user: {}", ledgerId, userId);
        
        AccountLedger ledger = accountLedgerRepository.findByIdAndIsDeletedFalse(ledgerId)
                .orElseThrow(() -> new RuntimeException("账本不存在，ID: " + ledgerId));
        
        if (!ledgerMemberRepository.existsByLedgerIdAndUserIdAndIsDeletedFalse(ledgerId, userId)) {
            throw new RuntimeException("无权访问该账本成员列表");
        }
        
        List<LedgerMember> members = ledgerMemberRepository
                .findByLedgerIdAndJoinStatusAndIsDeletedFalse(ledgerId, LedgerMember.JoinStatus.JOINED);
        
        return members.stream()
                .map(MemberResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
