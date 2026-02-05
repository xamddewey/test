package com.xdw.demobackend.repository;

import com.xdw.demobackend.entity.Invitation;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JRepository<Invitation, Long> {
    
    Optional<Invitation> findByIdAndIsDeletedFalse(Long id);
    
    List<Invitation> findByLedgerIdAndIsDeletedFalse(Long ledgerId);
    
    List<Invitation> findByRecipientIdAndIsDeletedFalse(Long recipientId);
    
    List<Invitation> findByRecipientIdAndStatusAndIsDeletedFalse(Long recipientId, Invitation.InvitationStatus status);
    
    List<Invitation> findBySenderIdAndIsDeletedFalse(Long senderId);
    
    List<Invitation> findByLedgerIdAndStatusAndIsDeletedFalse(Long ledgerId, Invitation.InvitationStatus status);
    
    Optional<Invitation> findByLedgerIdAndRecipientIdAndIsDeletedFalse(Long ledgerId, Long recipientId);
    
    boolean existsByLedgerIdAndRecipientIdAndStatusAndIsDeletedFalse(Long ledgerId, Long recipientId, Invitation.InvitationStatus status);
    
    long countByRecipientIdAndStatusAndIsDeletedFalse(Long recipientId, Invitation.InvitationStatus status);
}
