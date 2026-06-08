package com.logistics.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BorrowReturnRequest {
    private LocalDate actualReturnDate;
    private String remark;
}
