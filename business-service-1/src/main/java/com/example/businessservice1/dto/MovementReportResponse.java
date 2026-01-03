package com.example.businessservice1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для отчета по движению оборудования
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovementReportResponse {
    
    private Long historyId;
    private LocalDateTime changeDate;
    
    // Информация об оборудовании
    private Long equipmentId;
    private String inventoryNumber;
    private String serialNumber;
    private String manufacturer;
    private String categoryName;
    
    // Информация о перемещении
    private String fromCustodianName;
    private String fromCustodianPosition;
    private String toCustodianName;
    private String toCustodianPosition;
    
    // Изменение статуса
    private String oldStatusName;
    private String newStatusName;
    
    // Детали
    private String details;
}
