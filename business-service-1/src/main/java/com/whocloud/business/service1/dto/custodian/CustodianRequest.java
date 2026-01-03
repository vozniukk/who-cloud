package com.whocloud.business.service1.dto.custodian;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustodianRequest {

    /**
     * Optional link to global User ID from auth-service.
     * Example: User kvoznjuk@gmail.com (ID=123) -> Custodian vozniukk (authUserId=123)
     */
    private Long authUserId;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Size(max = 100, message = "Position must not exceed 100 characters")
    private String position;

    @Pattern(regexp = "^\\+?[0-9]{10,20}$", message = "Phone number must be valid")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @NotBlank(message = "Identification number is required")
    @Size(max = 50, message = "Identification number must not exceed 50 characters")
    private String identificationNumber;

    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date must be in the past or present")
    private LocalDate hireDate;

    @Future(message = "Contract end date must be in the future")
    private LocalDate contractEndDate;

    @NotNull(message = "Contract type ID is required")
    private Long contractTypeId;

    @NotNull(message = "Status ID is required")
    private Long statusId;
}
