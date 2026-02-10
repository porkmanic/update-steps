package com.example.updatesteps.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class UpdateStepRequest {
    @NotBlank(message = "步骤内容不能为空")
    private String content;
}
