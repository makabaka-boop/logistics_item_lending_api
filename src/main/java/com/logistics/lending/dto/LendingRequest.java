package com.logistics.lending.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LendingRequest {
    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    @NotNull(message = "借用人姓名不能为空")
    private String borrowerName;

    private String borrowerDept;
    private String borrowerPhone;

    @NotNull(message = "借用数量不能为空")
    @Min(value = 1, message = "借用数量至少为1")
    private Integer quantity;

    private String purpose;
    private LocalDateTime expectedReturnDate;
}
