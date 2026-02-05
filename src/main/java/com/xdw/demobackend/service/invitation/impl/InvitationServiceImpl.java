package com.xdw.demobackend.service.invitation.impl;

import com.xdw.demobackend.dto.invitation.InviteMemberRequest;
import com.xdw.demobackend.dto.invitation.InvitationResponse;
import com.xdw.demobackend.entity.*;
import com.xdw.demobackend.repository.AccountLedgerRepository;
import com.xdw.demobackend.repository.InvitationRepository;
import com.xdw.demobackend.repository.LedgerMemberRepository;
import com.xdw.demobackend.repository.UserRepository;
import com.xdw.demobackend.service.invitation.InvitationService;
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
public class InvitationServiceImpl implements InvitationService {
    
    private final InvitationRepository invitationRepository;
    private final AccountLedgerRepository accountLedgerRepository;
    private final LedgerMemberRepository ledgerMemberRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public InvitationResponse sendInvitation(InviteMemberRequest request, Long senderId) {
        
        Long ledgerId = request.getLedgerId();
        Long recipientId = request.getRecipientId();
        
        if (senderId.equals(recipientId)) {
            throw new RuntimeException("不能邀请自己");
        }
        
        AccountLedger ledger = accountLedgerRepository.findByIdAndIsDeletedFalse(ledgerId)
                .orElseThrow(() -> new RuntimeException("账本不存在，ID: " + ledgerId));
        
        if (!ledgerMemberRepository.existsByLedgerIdAndUserIdAndIsDeletedFalse(ledgerId, senderId)) {
            throw new RuntimeException("只有成员可以发送邀请");
        }
        
        if (ledgerMemberRepository.existsByLedgerIdAndUserIdAndIsDeletedFalse(ledgerId, recipientId)) {
            throw new RuntimeException("用户已经是成员");
        }
        
        if (invitationRepository.existsByLedgerIdAndRecipientIdAndStatusAndIsDeletedFalse(
                ledgerId, recipientId, Invitation.InvitationStatus.PENDING)) {
            throw new RuntimeException("已存在待处理的邀请");
        }
        
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("发送者不存在，ID: " + senderId));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("接收者不存在，ID: " + recipientId));
        
        LocalDateTime now = LocalDateTime.now();
        
        Invitation invitation = invitationRepository.insert(
                InvitationDraft.$.produce(draft -> {
                    draft.setLedger(AccountLedgerDraft.$.produce(l -> l.setId(ledgerId)));
                    draft.setSender(UserDraft.$.produce(s -> s.setId(senderId)));
                    draft.setRecipient(UserDraft.$.produce(r -> r.setId(recipientId)));
                    draft.setLedgerName(ledger.ledgerName());
                    draft.setSenderNickname(sender.nickname() != null ? sender.nickname() : sender.username());
                    draft.setRecipientNickname(recipient.nickname() != null ? recipient.nickname() : recipient.username());
                    draft.setStatus(Invitation.InvitationStatus.PENDING);
                    draft.setCreatedAt(now);
                    draft.setUpdatedAt(now);
                    draft.setIsDeleted(false);
                })
        );
        
        AccountLedger updatedLedger = AccountLedgerDraft.$.produce(ledger, draft -> {
            draft.setInvitedCount((ledger.invitedCount() != null ? ledger.invitedCount() : 0) + 1);
            draft.setLastActivityAt(now);
        });
        accountLedgerRepository.save(updatedLedger);
        
        return InvitationResponse.fromEntity(invitation);
    }
    
    @Override
    @Transactional
    public InvitationResponse acceptInvitation(Long invitationId, Long userId) {
        
        Invitation invitation = invitationRepository.findByIdAndIsDeletedFalse(invitationId)
                .orElseThrow(() -> new RuntimeException("邀请不存在，ID: " + invitationId));
        
        if (invitation.recipientId() != userId) {
            throw new RuntimeException("只有受邀者可以接受邀请");
        }
        
        if (invitation.status() != Invitation.InvitationStatus.PENDING) {
            throw new RuntimeException("邀请状态不是待处理");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        Invitation accepted = InvitationDraft.$.produce(invitation, draft -> {
            draft.setStatus(Invitation.InvitationStatus.ACCEPTED);
            draft.setUpdatedAt(now);
        });
        invitationRepository.save(accepted);
        
        AccountLedger ledger = accountLedgerRepository.findByIdAndIsDeletedFalse(invitation.ledgerId())
                .orElseThrow(() -> new RuntimeException("账本不存在"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        var existingMember = ledgerMemberRepository.findByLedgerIdAndUserIdAndIsDeletedFalse(
                invitation.ledgerId(), userId);
        
        if (existingMember.isPresent()) {
            LedgerMember member = existingMember.get();
            LedgerMember updatedMember = LedgerMemberDraft.$.produce(member, draft -> {
                draft.setJoinStatus(LedgerMember.JoinStatus.JOINED);
                draft.setJoinedAt(now);
                draft.setLastActivityAt(now);
            });
            ledgerMemberRepository.save(updatedMember);
        } else {
            ledgerMemberRepository.insert(
                    LedgerMemberDraft.$.produce(draft -> {
                        draft.setLedger(AccountLedgerDraft.$.produce(l -> l.setId(invitation.ledgerId())));
                        draft.setUser(UserDraft.$.produce(u -> u.setId(userId)));
                        draft.setLedgerName(ledger.ledgerName());
                        draft.setUserNickname(user.nickname() != null ? user.nickname() : user.username());
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
        }
        
        AccountLedger updatedLedger = AccountLedgerDraft.$.produce(ledger, draft -> {
            draft.setMemberCount((ledger.memberCount() != null ? ledger.memberCount() : 0) + 1);
            draft.setInvitedCount((ledger.invitedCount() != null ? ledger.invitedCount() : 0) - 1);
            draft.setLastActivityAt(now);
        });
        accountLedgerRepository.save(updatedLedger);
        
        return InvitationResponse.fromEntity(accepted);
    }
    
    @Override
    @Transactional
    public InvitationResponse rejectInvitation(Long invitationId, Long userId) {
        
        Invitation invitation = invitationRepository.findByIdAndIsDeletedFalse(invitationId)
                .orElseThrow(() -> new RuntimeException("邀请不存在，ID: " + invitationId));
        
        if (invitation.recipientId() != userId) {
            throw new RuntimeException("只有受邀者可以拒绝邀请");
        }
        
        if (invitation.status() != Invitation.InvitationStatus.PENDING) {
            throw new RuntimeException("邀请状态不是待处理");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        Invitation rejected = InvitationDraft.$.produce(invitation, draft -> {
            draft.setStatus(Invitation.InvitationStatus.REJECTED);
            draft.setUpdatedAt(now);
        });
        invitationRepository.save(rejected);
        
        AccountLedger ledger = accountLedgerRepository.findByIdAndIsDeletedFalse(invitation.ledgerId())
                .orElseThrow(() -> new RuntimeException("账本不存在"));
        
        AccountLedger updatedLedger = AccountLedgerDraft.$.produce(ledger, draft -> {
            draft.setInvitedCount((ledger.invitedCount() != null ? ledger.invitedCount() : 0) - 1);
            draft.setLastActivityAt(now);
        });
        accountLedgerRepository.save(updatedLedger);
        
        return InvitationResponse.fromEntity(rejected);
    }
    
    @Override
    public List<InvitationResponse> getMyPendingInvitations(Long userId) {
        
        List<Invitation> invitations = invitationRepository
                .findByRecipientIdAndStatusAndIsDeletedFalse(userId, Invitation.InvitationStatus.PENDING);
        
        return invitations.stream()
                .map(InvitationResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<InvitationResponse> getLedgerInvitations(Long ledgerId, Long userId) {
        
        AccountLedger ledger = accountLedgerRepository.findByIdAndIsDeletedFalse(ledgerId)
                .orElseThrow(() -> new RuntimeException("账本不存在，ID: " + ledgerId));
        
        if (!ledgerMemberRepository.existsByLedgerIdAndUserIdAndIsDeletedFalse(ledgerId, userId)) {
            throw new RuntimeException("无权查看该账本的邀请列表");
        }
        
        List<Invitation> invitations = invitationRepository.findByLedgerIdAndIsDeletedFalse(ledgerId);
        
        return invitations.stream()
                .map(InvitationResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
