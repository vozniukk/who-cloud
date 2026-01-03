package com.whocloud.business.service1.dto.custodian;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustodianResponse {
    
    private Long id;
    
    /**
     * Link to global User ID from auth-service (if linked).
     */
    private Long authUserId;
    
    private String firstName;
    private String lastName;
    private String fullName;
    private String position;
    private String phone;
    private String identificationNumber;
    private LocalDate hireDate;
    private LocalDate contractEndDate;
    private Long contractTypeId;
    private String contractTypeName;
    private Long statusId;
    private String statusName;
    private Integer equipmentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
