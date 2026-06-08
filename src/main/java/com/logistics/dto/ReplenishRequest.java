package com.logistics.dto;

import lombok.Data;

@Data
public class ReplenishRequest {
    private Integer addQty;
    private String remark;
}
