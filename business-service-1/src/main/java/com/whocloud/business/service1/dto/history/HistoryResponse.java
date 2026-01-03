package com.whocloud.business.service1.dto.history;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryResponse {
    
    private Long id;
    private String actionType;
    private LocalDateTime timestamp;
    private Long fromCustodianId;
    private String fromCustodianName;
    private Long toCustodianId;
    private String toCustodianName;
    private String details;
    private Long userId; // Who performed the action
    private String ipAddress;
}
