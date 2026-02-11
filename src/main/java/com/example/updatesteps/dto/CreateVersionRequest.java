package com.example.updatesteps.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateVersionRequest {
    @NotBlank(message = "版本号不能为空")
    private String versionNumber;

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }
}
