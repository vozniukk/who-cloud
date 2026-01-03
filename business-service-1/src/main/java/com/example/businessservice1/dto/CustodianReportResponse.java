package com.example.businessservice1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO для информации о материально-ответственном лице в отчете
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustodianReportResponse {
    
    private Long id;
    private Long authUserId;
    private String firstName;
    private String lastName;
    private String identificationNumber;
    private String position;
    private LocalDate hireDate;
    
    // Статус
    private String statusName;
    
    // Статистика по оборудованию
    private Integer equipmentCount;
    private Integer activeEquipmentCount;
    private Integer inRepairEquipmentCount;
    private Integer retiredEquipmentCount;
    
    // Метаданные
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
