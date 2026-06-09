package com.logistics.dto;

import lombok.Data;

@Data
public class RepairRequest {
    private Long itemId;
    private Long borrowRecordId;
    private String description;
    private String reporter;
    private String repairPerson;
    private String remark;
}
