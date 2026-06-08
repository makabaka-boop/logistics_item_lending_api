package com.logistics.lending.dto;

import lombok.Data;

@Data
public class MaintenanceReviewRequest {
    private Boolean approved;
    private String reviewRemark;
}
