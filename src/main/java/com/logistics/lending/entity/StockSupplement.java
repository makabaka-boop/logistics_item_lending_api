package com.logistics.lending.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stock_supplement")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class StockSupplement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    @JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
    private Item item;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false, length = 20)
    private String status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "submitted_by")
    @JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "password"})
    private User submittedBy;

    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by")
    @JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "password"})
    private User approvedBy;

    private LocalDateTime approvedAt;

    @Column(length = 500)
    private String approveRemark;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
