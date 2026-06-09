package com.logistics.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RepairQueryRequest {
    private Long itemId;
    private Long borrowRecordId;
    private String status;
    private String reporter;
    private String repairPerson;
    private LocalDate startDate;
    private LocalDate endDate;
}
