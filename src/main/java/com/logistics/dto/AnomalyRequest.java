package com.logistics.dto;

import lombok.Data;

@Data
public class AnomalyRequest {
    private Long itemId;
    private Long borrowRecordId;
    private String type;
    private String description;
    private String reporter;
}
