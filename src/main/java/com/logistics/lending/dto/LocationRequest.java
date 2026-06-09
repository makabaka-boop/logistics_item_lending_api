package com.logistics.lending.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LocationRequest {
    @NotBlank(message = "位置名称不能为空")
    private String name;
    private String description;
}
