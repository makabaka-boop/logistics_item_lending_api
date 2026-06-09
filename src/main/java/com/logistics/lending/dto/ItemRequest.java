package com.logistics.lending.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemRequest {
    @NotBlank(message = "物品编码不能为空")
    private String itemCode;

    @NotBlank(message = "物品名称不能为空")
    private String name;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotNull(message = "存放位置不能为空")
    private Long locationId;

    @NotNull(message = "总数量不能为空")
    @Min(value = 0, message = "总数量不能小于0")
    private Integer totalQuantity;

    private String responsiblePerson;
    private String remark;
    private Integer warningThreshold = 5;
}
