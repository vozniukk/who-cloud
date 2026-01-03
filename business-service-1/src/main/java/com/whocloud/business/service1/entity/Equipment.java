package com.whocloud.business.service1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment", indexes = {
    @Index(name = "idx_equipment_inventory_number", columnList = "inventory_number"),
    @Index(name = "idx_equipment_serial_number", columnList = "serial_number"),
    @Index(name = "idx_equipment_status", columnList = "status_id"),
    @Index(name = "idx_equipment_category", columnList = "category_id"),
    @Index(name = "idx_equipment_custodian", columnList = "current_custodian_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String manufacturer;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "commissioning_date", nullable = false)
    private LocalDate commissioningDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    private ObjectStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_custodian_id")
    private Custodian currentCustodian;

    @Column(name = "inventory_number", unique = true, nullable = false, length = 50)
    private String inventoryNumber;

    @Column(name = "serial_number", unique = true, length = 50)
    private String serialNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

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

    public boolean isInWarehouse() {
        return currentCustodian == null;
    }
}
