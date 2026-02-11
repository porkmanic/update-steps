package com.example.updatesteps.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateStepRequest {
    @NotNull(message = "版本ID不能为空")
    private Long versionId;

    @NotBlank(message = "步骤内容不能为空")
    private String content;

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
