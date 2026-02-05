package com.xdw.demobackend.dto.expense;

import com.xdw.demobackend.entity.ExpenseRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 记账条目响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    
    private Long id;
    private Long ledgerId;
    private String ledgerName;
    private Long categoryId;
    private String categoryName;
    private Long payerId;
    private String payerNickname;
    private BigDecimal amount;
    private String description;
    private LocalDate expenseDate;
    private Long createdById;
    private String creatorNickname;
    private Integer participantCount;
    private BigDecimal avgAmount;
    private Boolean hasSettlement;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ParticipantResponse> participants;
    
    public static ExpenseResponse fromEntity(ExpenseRecord record) {
        return ExpenseResponse.builder()
                .id(record.id())
                .ledgerId(record.ledgerId())
                .ledgerName(record.ledgerName())
                .categoryId(record.categoryId())
                .categoryName(record.categoryName())
                .payerId(record.payerId())
                .payerNickname(record.payerNickname())
                .amount(record.amount())
                .description(record.description())
                .expenseDate(record.expenseDate())
                .createdById(record.createdById())
                .creatorNickname(record.creatorNickname())
                .participantCount(record.participantCount())
                .avgAmount(record.avgAmount())
                .hasSettlement(record.hasSettlement())
                .createdAt(record.createdAt())
                .updatedAt(record.updatedAt())
                .participants(record.participants() != null ? 
                    record.participants().stream()
                        .map(ParticipantResponse::fromEntity)
                        .collect(Collectors.toList()) : null)
                .build();
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantResponse {
        private Long id;
        private Long userId;
        private String userNickname;
        private BigDecimal amount;
        
        public static ParticipantResponse fromEntity(com.xdw.demobackend.entity.ExpenseParticipant participant) {
            return ParticipantResponse.builder()
                    .id(participant.id())
                    .userId(participant.userId())
                    .userNickname(participant.userNickname())
                    .amount(participant.amount())
                    .build();
        }
    }
}
