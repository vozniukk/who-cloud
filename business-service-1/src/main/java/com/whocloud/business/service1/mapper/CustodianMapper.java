package com.whocloud.business.service1.mapper;

import com.whocloud.business.service1.dto.custodian.CustodianRequest;
import com.whocloud.business.service1.dto.custodian.CustodianResponse;
import com.whocloud.business.service1.entity.Custodian;
import org.springframework.stereotype.Component;

@Component
public class CustodianMapper {

    public CustodianResponse toResponse(Custodian custodian) {
        if (custodian == null) {
            return null;
        }

        return CustodianResponse.builder()
                .id(custodian.getId())
                .authUserId(custodian.getAuthUserId())
                .firstName(custodian.getFirstName())
                .lastName(custodian.getLastName())
                .fullName(custodian.getFullName())
                .position(custodian.getPosition())
                .phone(custodian.getPhone())
                .identificationNumber(custodian.getIdentificationNumber())
                .hireDate(custodian.getHireDate())
                .contractEndDate(custodian.getContractEndDate())
                .contractTypeId(custodian.getContractType().getId())
                .contractTypeName(custodian.getContractType().getName())
                .statusId(custodian.getStatus().getId())
                .statusName(custodian.getStatus().getName())
                .equipmentCount(custodian.getEquipments().size())
                .createdAt(custodian.getCreatedAt())
                .updatedAt(custodian.getUpdatedAt())
                .build();
    }

    public Custodian toEntity(CustodianRequest request) {
        if (request == null) {
            return null;
        }

        return Custodian.builder()
                .authUserId(request.getAuthUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .position(request.getPosition())
                .phone(request.getPhone())
                .identificationNumber(request.getIdentificationNumber())
                .hireDate(request.getHireDate())
                .contractEndDate(request.getContractEndDate())
                .build();
    }

    public void updateEntityFromRequest(Custodian custodian, CustodianRequest request) {
        if (custodian == null || request == null) {
            return;
        }

        custodian.setAuthUserId(request.getAuthUserId());
        custodian.setFirstName(request.getFirstName());
        custodian.setLastName(request.getLastName());
        custodian.setPosition(request.getPosition());
        custodian.setPhone(request.getPhone());
        custodian.setIdentificationNumber(request.getIdentificationNumber());
        custodian.setHireDate(request.getHireDate());
        custodian.setContractEndDate(request.getContractEndDate());
    }
}
