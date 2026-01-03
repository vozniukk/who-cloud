package com.whocloud.business.service1.mapper;

import com.whocloud.business.service1.dto.equipment.EquipmentRequest;
import com.whocloud.business.service1.dto.equipment.EquipmentResponse;
import com.whocloud.business.service1.entity.Equipment;
import org.springframework.stereotype.Component;

@Component
public class EquipmentMapper {

    public EquipmentResponse toResponse(Equipment equipment) {
        return EquipmentResponse.builder()
                .id(equipment.getId())
                .manufacturer(equipment.getManufacturer())
                .releaseDate(equipment.getReleaseDate())
                .commissioningDate(equipment.getCommissioningDate())
                .statusId(equipment.getStatus().getId())
                .statusName(equipment.getStatus().getName())
                .currentCustodianId(equipment.getCurrentCustodian() != null ? equipment.getCurrentCustodian().getId() : null)
                .currentCustodianName(equipment.getCurrentCustodian() != null ? equipment.getCurrentCustodian().getFullName() : "Warehouse")
                .inventoryNumber(equipment.getInventoryNumber())
                .serialNumber(equipment.getSerialNumber())
                .categoryId(equipment.getCategory().getId())
                .categoryName(equipment.getCategory().getName())
                .createdAt(equipment.getCreatedAt())
                .updatedAt(equipment.getUpdatedAt())
                .build();
    }

    public Equipment toEntity(EquipmentRequest request) {
        return Equipment.builder()
                .manufacturer(request.getManufacturer())
                .releaseDate(request.getReleaseDate())
                .commissioningDate(request.getCommissioningDate())
                .inventoryNumber(request.getInventoryNumber())
                .serialNumber(request.getSerialNumber())
                .build();
    }

    public void updateEntityFromRequest(Equipment equipment, EquipmentRequest request) {
        equipment.setManufacturer(request.getManufacturer());
        equipment.setReleaseDate(request.getReleaseDate());
        equipment.setCommissioningDate(request.getCommissioningDate());
        equipment.setInventoryNumber(request.getInventoryNumber());
        equipment.setSerialNumber(request.getSerialNumber());
    }
}
