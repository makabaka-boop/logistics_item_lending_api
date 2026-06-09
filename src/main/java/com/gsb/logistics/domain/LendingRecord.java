package com.gsb.logistics.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "lending_record")
@Getter
@Setter
public class LendingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "item_id")
    private Long itemId;

    /** 借用人 */
    @Column(nullable = false, length = 64)
    private String borrower;

    /** 借用数量 */
    @Column(nullable = false)
    private Integer quantity = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LendingStatus status = LendingStatus.APPLIED;

    /** 申请说明 */
    @Column(length = 500)
    private String applyReason;

    /** 申请时间 */
    @Column(nullable = false)
    private LocalDateTime applyTime = LocalDateTime.now();

    /** 借出时间 */
    private LocalDateTime lendTime;

    /** 期望归还时间 */
    private LocalDateTime expectReturnTime;

    /** 实际归还时间 */
    private LocalDateTime returnTime;

    /** 归还说明 */
    @Column(length = 500)
    private String returnRemark;

    /** 异常类型 */
    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private AbnormalType abnormalType = AbnormalType.NONE;

    /** 异常说明 */
    @Column(length = 500)
    private String abnormalRemark;

    /** 异常确认人 */
    @Column(length = 64)
    private String abnormalConfirmedBy;

    /** 异常确认时间 */
    private LocalDateTime abnormalConfirmedAt;
}
