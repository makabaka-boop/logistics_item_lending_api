package com.logistics.dto;

import lombok.Data;

@Data
public class ItemRequest {
    private String name;
    private Long categoryId;
    private Long locationId;
    private Integer totalQty;
    private Integer availableQty;
    private String responsiblePerson;
    private String status;
}
