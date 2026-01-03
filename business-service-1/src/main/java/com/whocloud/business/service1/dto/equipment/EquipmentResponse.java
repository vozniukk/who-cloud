package com.whocloud.business.service1.dto.equipment;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentResponse {
    
    private Long id;
    private String manufacturer;
    private LocalDate releaseDate;
    private LocalDate commissioningDate;
    private Long statusId;
    private String statusName;
    private Long currentCustodianId;
    private String currentCustodianName;
    private String inventoryNumber;
    private String serialNumber;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
