package com.gsb.logistics.domain;

public enum LendingStatus {
    /** 已申请，等待出借确认 */
    APPLIED,
    /** 已借出 */
    LENT,
    /** 已归还（正常） */
    RETURNED,
    /** 逾期未归还 */
    OVERDUE,
    /** 异常归还（损坏/丢失等） */
    ABNORMAL,
    /** 已驳回 */
    REJECTED
}
