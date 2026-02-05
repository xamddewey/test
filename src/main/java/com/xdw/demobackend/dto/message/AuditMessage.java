package com.xdw.demobackend.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditMessage {
    private String entityType;
    private Long entityId;
    private String action;
    private Long actorId;
    private String changes;
    private Long ledgerId;
}
