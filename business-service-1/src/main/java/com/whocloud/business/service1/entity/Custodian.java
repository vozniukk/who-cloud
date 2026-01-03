package com.whocloud.business.service1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Custodian entity represents a person who is responsible for equipment.
 * This is a local entity within business-service-1 and can be linked to a global User 
 * from auth-service via authUserId field.
 * 
 * Example: kvoznjuk@gmail.com (global User) -> vozniukk (local Custodian account)
 */
@Entity
@Table(name = "custodians", indexes = {
    @Index(name = "idx_custodian_identification_number", columnList = "identification_number"),
    @Index(name = "idx_custodian_status", columnList = "status_id"),
    @Index(name = "idx_custodian_auth_user", columnList = "auth_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Custodian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Link to global User ID from auth-service.
     * Allows to bind local custodian account to global user account.
     * Example: User kvoznjuk@gmail.com with ID=123 -> Custodian vozniukk with authUserId=123
     */
    @Column(name = "auth_user_id")
    private Long authUserId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 100)
    private String position;

    @Column(length = 20)
    private String phone;

    @Column(name = "identification_number", unique = true, nullable = false, length = 50)
    private String identificationNumber;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contract_type_id", nullable = false)
    private ContractType contractType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    private CustodianStatus status;

    @OneToMany(mappedBy = "currentCustodian", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<Equipment> equipments = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
