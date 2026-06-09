package com.logistics.lending.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "maintenance_record")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class MaintenanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    @JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
    private Item item;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lending_record_id")
    @JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "item", "lentBy", "receivedBy", "abnormalConfirmedBy"})
    private LendingRecord lendingRecord;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 500)
    private String problemDescription;

    @Column(length = 500)
    private String maintenanceResult;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "submitted_by")
    @JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "password"})
    private User submittedBy;

    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewed_by")
    @JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "password"})
    private User reviewedBy;

    private LocalDateTime reviewedAt;

    @Column(length = 500)
    private String reviewRemark;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
