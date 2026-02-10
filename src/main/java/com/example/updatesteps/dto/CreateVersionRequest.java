package com.example.updatesteps.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CreateVersionRequest {
    @NotBlank(message = "版本号不能为空")
    private String versionNumber;
}
