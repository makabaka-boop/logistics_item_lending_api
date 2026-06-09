package com.logistics.lending.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MaintenanceRequest {
    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    @NotNull(message = "维修数量不能为空")
    @Min(value = 1, message = "维修数量至少为1")
    private Integer quantity;

    private String problemDescription;
}
