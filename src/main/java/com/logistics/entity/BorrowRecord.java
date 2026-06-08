package com.logistics.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "borrow_record")
public class BorrowRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false, length = 50)
    private String borrowerName;

    @Column(length = 100)
    private String borrowerDept;

    @Column(nullable = false)
    private Integer qty;

    @Column(nullable = false, length = 20)
    private String status;

    private LocalDate borrowDate;

    private LocalDate expectedReturnDate;

    private LocalDate actualReturnDate;

    @Column(length = 50)
    private String approver;

    @Column(length = 500)
    private String remark;

    @Column(length = 500)
    private String anomalyRemark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
