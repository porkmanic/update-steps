package com.example.updatesteps.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateStepRequest {
    @NotBlank(message = "步骤内容不能为空")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
