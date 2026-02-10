package com.example.updatesteps.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CreateStepRequest {
    @NotNull(message = "版本ID不能为空")
    private Long versionId;

    @NotBlank(message = "步骤内容不能为空")
    private String content;
}
