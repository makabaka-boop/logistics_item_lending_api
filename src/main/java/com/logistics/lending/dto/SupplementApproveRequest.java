package com.logistics.lending.dto;

import lombok.Data;

@Data
public class SupplementApproveRequest {
    private Boolean approved;
    private String approveRemark;
}
