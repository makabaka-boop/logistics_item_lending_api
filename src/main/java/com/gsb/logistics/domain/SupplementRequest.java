package com.gsb.logistics.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 补充申请：用于针对某个借用记录追加申请数量或追加说明
 */
@Entity
@Table(name = "supplement_request")
@Getter
@Setter
public class SupplementRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "lending_id")
    private Long lendingId;

    @Column(nullable = false)
    private Integer extraQuantity = 0;

    @Column(length = 500)
    private String reason;

    /** 提交人 */
    @Column(length = 64)
    private String submittedBy;

    @Column(length = 32)
    private String status = "PENDING"; // PENDING / APPROVED / REJECTED

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime processedAt;

    @Column(length = 64)
    private String processedBy;

    @Column(length = 500)
    private String processRemark;
}
