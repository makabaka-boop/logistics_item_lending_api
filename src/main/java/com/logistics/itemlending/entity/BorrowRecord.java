package com.logistics.itemlending.entity;

import com.logistics.itemlending.enums.BorrowStatus;
import com.logistics.itemlending.enums.ExceptionType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "borrow_records")
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String recordNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    private User borrower;

    @Column(length = 500)
    private String borrowReason;

    @Column(nullable = false)
    private LocalDateTime expectedReturnTime;

    private LocalDateTime borrowTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_confirmer_id")
    private User borrowConfirmer;

    private LocalDateTime returnTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_confirmer_id")
    private User returnConfirmer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BorrowStatus status = BorrowStatus.PENDING;

    @Column(length = 500)
    private String returnRemark;

    private Boolean hasException = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ExceptionType exceptionType;

    @Column(length = 1000)
    private String exceptionDescription;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
