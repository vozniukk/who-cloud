package com.whocloud.business.service1.dto.equipment;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentRequest {

    @NotBlank(message = "Manufacturer is required")
    @Size(max = 255, message = "Manufacturer must not exceed 255 characters")
    private String manufacturer;

    @NotNull(message = "Release date is required")
    @PastOrPresent(message = "Release date must be in the past or present")
    private LocalDate releaseDate;

    @NotNull(message = "Commissioning date is required")
    @PastOrPresent(message = "Commissioning date must be in the past or present")
    private LocalDate commissioningDate;

    @NotNull(message = "Status ID is required")
    private Long statusId;

    private Long currentCustodianId; // Null for warehouse

    @NotBlank(message = "Inventory number is required")
    @Size(max = 50, message = "Inventory number must not exceed 50 characters")
    private String inventoryNumber;

    @Size(max = 50, message = "Serial number must not exceed 50 characters")
    private String serialNumber;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
