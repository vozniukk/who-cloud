package com.whocloud.business.service1.dto.equipment;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferEquipmentRequest {

    private Long toCustodianId; // Null for warehouse
    
    @NotNull(message = "Details are required for transfer")
    private String details;
}
