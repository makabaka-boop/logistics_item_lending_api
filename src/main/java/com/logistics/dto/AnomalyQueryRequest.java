package com.logistics.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AnomalyQueryRequest {
    private Long itemId;
    private Long borrowRecordId;
    private String type;
    private String status;
    private String reporter;
    private LocalDate startDate;
    private LocalDate endDate;
}
