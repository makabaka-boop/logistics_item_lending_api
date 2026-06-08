package com.logistics.lending.dto;

import lombok.Data;

@Data
public class ReturnRequest {
    private String returnRemark;
    private String abnormalType;
    private String abnormalRemark;
}
