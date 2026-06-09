package com.logistics.lending.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "lending_record")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class LendingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    @JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
    private Item item;

    @Column(nullable = false, length = 50)
    private String borrowerName;

    @Column(length = 50)
    private String borrowerDept;

    @Column(length = 20)
    private String borrowerPhone;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 500)
    private String purpose;

    private LocalDateTime expectedReturnDate;

    private LocalDateTime lentAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lent_by")
    @JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "password"})
    private User lentBy;

    private LocalDateTime returnedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "received_by")
    @JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "password"})
    private User receivedBy;

    @Column(length = 500)
    private String returnRemark;

    @Column(length = 50)
    private String abnormalType;

    @Column(length = 500)
    private String abnormalRemark;

    private Boolean abnormalConfirmed = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "abnormal_confirmed_by")
    @JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "password"})
    private User abnormalConfirmedBy;

    private LocalDateTime abnormalConfirmedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
