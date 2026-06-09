package com.gsb.logistics.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "repair_record")
@Getter
@Setter
public class RepairRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "item_id")
    private Long itemId;

    /** 关联的借用记录（可空） */
    @Column(name = "lending_id")
    private Long lendingId;

    /** 维修数量 */
    @Column(nullable = false)
    private Integer quantity = 1;

    /** 故障描述 */
    @Column(nullable = false, length = 500)
    private String issueDescription;

    /** 维修说明（处理过程） */
    @Column(length = 500)
    private String repairRemark;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RepairStatus status = RepairStatus.REGISTERED;

    /** 登记时间 */
    @Column(nullable = false)
    private LocalDateTime registerTime = LocalDateTime.now();

    /** 维修开始时间 */
    private LocalDateTime startTime;

    /** 维修完成时间 */
    private LocalDateTime finishTime;

    /** 复核时间 */
    private LocalDateTime reviewTime;

    /** 复核人 */
    @Column(length = 64)
    private String reviewer;

    /** 复核结论 */
    @Column(length = 500)
    private String reviewRemark;

    /** 复核是否超时未完成（异常标记） */
    @Column(nullable = false)
    private Boolean reviewOverdue = false;
}
