package com.logistics.dto;

import lombok.Data;

@Data
public class ItemQueryRequest {
    private Long categoryId;
    private Long locationId;
    private String status;
    private String responsiblePerson;
    private String name;
}
