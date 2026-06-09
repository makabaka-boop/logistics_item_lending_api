package com.logistics.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BorrowRequest {
    private Long itemId;
    private String borrowerName;
    private String borrowerDept;
    private Integer qty;
    private LocalDate expectedReturnDate;
    private String remark;
}
