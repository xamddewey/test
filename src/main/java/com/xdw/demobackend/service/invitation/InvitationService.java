package com.xdw.demobackend.service.invitation;

import com.xdw.demobackend.dto.invitation.InviteMemberRequest;
import com.xdw.demobackend.dto.invitation.InvitationResponse;

import java.util.List;

public interface InvitationService {
    
    InvitationResponse sendInvitation(InviteMemberRequest request, Long senderId);
    
    InvitationResponse acceptInvitation(Long invitationId, Long userId);
    
    InvitationResponse rejectInvitation(Long invitationId, Long userId);
    
    List<InvitationResponse> getMyPendingInvitations(Long userId);
    
    List<InvitationResponse> getLedgerInvitations(Long ledgerId, Long userId);
}
