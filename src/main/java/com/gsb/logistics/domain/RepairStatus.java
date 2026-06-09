package com.gsb.logistics.domain;

public enum RepairStatus {
    /** 已登记，待维修 */
    REGISTERED,
    /** 维修中 */
    IN_PROGRESS,
    /** 维修完成，待复核 */
    DONE_PENDING_REVIEW,
    /** 已复核（恢复入库） */
    REVIEWED,
    /** 报废 */
    SCRAPPED
}
