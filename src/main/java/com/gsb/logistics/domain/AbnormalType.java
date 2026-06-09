package com.gsb.logistics.domain;

public enum AbnormalType {
    /** 无异常 */
    NONE,
    /** 逾期 */
    OVERDUE,
    /** 损坏 */
    DAMAGED,
    /** 丢失 */
    LOST,
    /** 维修后未复核 */
    REPAIR_NOT_REVIEWED,
    /** 连续异常归还 */
    REPEATED_ABNORMAL,
    /** 其他 */
    OTHER
}
