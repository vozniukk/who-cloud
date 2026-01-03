package com.whocloud.business.service1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "custodian_history", indexes = {
    @Index(name = "idx_custodian_history_custodian", columnList = "custodian_id"),
    @Index(name = "idx_custodian_history_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustodianHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custodian_id", nullable = false)
    private Custodian custodian;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType; // CREATED, UPDATED, DEACTIVATED, ACTIVATED

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "modified_by_user_id")
    private Long modifiedByUserId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
