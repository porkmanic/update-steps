package com.example.updatesteps.dto;

import java.time.LocalDateTime;

public class VersionResponse {
    private Long id;
    private String versionNumber;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime uatAt;
    private LocalDateTime archivedAt;
    private int stepCount;
    private int newStepCount; // UAT后新增步骤数

    public VersionResponse() {
    }

    public VersionResponse(Long id, String versionNumber, String status, LocalDateTime createdAt, LocalDateTime uatAt,
            LocalDateTime archivedAt, int stepCount, int newStepCount) {
        this.id = id;
        this.versionNumber = versionNumber;
        this.status = status;
        this.createdAt = createdAt;
        this.uatAt = uatAt;
        this.archivedAt = archivedAt;
        this.stepCount = stepCount;
        this.newStepCount = newStepCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUatAt() {
        return uatAt;
    }

    public void setUatAt(LocalDateTime uatAt) {
        this.uatAt = uatAt;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(LocalDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public int getNewStepCount() {
        return newStepCount;
    }

    public void setNewStepCount(int newStepCount) {
        this.newStepCount = newStepCount;
    }
}
