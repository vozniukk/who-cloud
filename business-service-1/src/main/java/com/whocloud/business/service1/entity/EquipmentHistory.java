package com.whocloud.business.service1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_history", indexes = {
    @Index(name = "idx_equipment_history_equipment", columnList = "equipment_id"),
    @Index(name = "idx_equipment_history_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType; // CREATED, UPDATED, TRANSFERRED, STATUS_CHANGED

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "from_custodian_id")
    private Long fromCustodianId;

    @Column(name = "to_custodian_id")
    private Long toCustodianId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "user_id")
    private Long userId; // ID пользователя (из auth-service), который выполнил действие

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
