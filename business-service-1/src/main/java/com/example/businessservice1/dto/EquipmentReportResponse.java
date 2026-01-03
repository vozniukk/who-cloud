package com.example.businessservice1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO для детальной информации об оборудовании в отчете
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentReportResponse {
    
    private Long id;
    private String inventoryNumber;
    private String serialNumber;
    private String manufacturer;
    private LocalDate releaseDate;
    private LocalDate commissioningDate;
    private BigDecimal initialCost;
    private String currency;
    
    // Справочные данные
    private String statusName;
    private Map<String, String> statusTranslations;
    
    private String categoryName;
    private Map<String, String> categoryTranslations;
    
    private String contractTypeName;
    private Map<String, String> contractTypeTranslations;
    
    // Информация о текущем владельце
    private String currentOwnerName;
    private String currentOwnerPosition;
    private String currentOwnerIdentificationNumber;
    
    // История (опционально)
    private Integer transfersCount;
    private LocalDateTime lastTransferDate;
    
    // Метаданные
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
