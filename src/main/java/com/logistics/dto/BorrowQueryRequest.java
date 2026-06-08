package com.logistics.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BorrowQueryRequest {
    private Long itemId;
    private Long categoryId;
    private Long locationId;
    private String borrowerName;
    private String borrowerDept;
    private String status;
    private String approver;
    private Boolean hasAnomaly;
    private LocalDate startDate;
    private LocalDate endDate;
}
