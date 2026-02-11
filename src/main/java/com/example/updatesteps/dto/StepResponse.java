package com.example.updatesteps.dto;

import java.time.LocalDateTime;
import java.util.List;

public class StepResponse {
    private Long id;
    private Long versionId;
    private String content;
    private String username;
    private String userDisplayName; // 用户显示名称
    private Long userId;
    private Boolean locked;
    private Boolean afterUat;
    private Boolean uatConfirmed; // UAT已执行确认
    private LocalDateTime uatConfirmedAt;
    private Boolean prodConfirmed; // 生产已执行确认
    private LocalDateTime prodConfirmedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AttachmentResponse> attachments;

    public StepResponse() {
    }

    public StepResponse(Long id, Long versionId, String content, String username, String userDisplayName, Long userId,
            Boolean locked, Boolean afterUat, Boolean uatConfirmed, LocalDateTime uatConfirmedAt, Boolean prodConfirmed,
            LocalDateTime prodConfirmedAt, LocalDateTime createdAt, LocalDateTime updatedAt,
            List<AttachmentResponse> attachments) {
        this.id = id;
        this.versionId = versionId;
        this.content = content;
        this.username = username;
        this.userDisplayName = userDisplayName;
        this.userId = userId;
        this.locked = locked;
        this.afterUat = afterUat;
        this.uatConfirmed = uatConfirmed;
        this.uatConfirmedAt = uatConfirmedAt;
        this.prodConfirmed = prodConfirmed;
        this.prodConfirmedAt = prodConfirmedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.attachments = attachments;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Boolean getAfterUat() {
        return afterUat;
    }

    public void setAfterUat(Boolean afterUat) {
        this.afterUat = afterUat;
    }

    public Boolean getUatConfirmed() {
        return uatConfirmed;
    }

    public void setUatConfirmed(Boolean uatConfirmed) {
        this.uatConfirmed = uatConfirmed;
    }

    public LocalDateTime getUatConfirmedAt() {
        return uatConfirmedAt;
    }

    public void setUatConfirmedAt(LocalDateTime uatConfirmedAt) {
        this.uatConfirmedAt = uatConfirmedAt;
    }

    public Boolean getProdConfirmed() {
        return prodConfirmed;
    }

    public void setProdConfirmed(Boolean prodConfirmed) {
        this.prodConfirmed = prodConfirmed;
    }

    public LocalDateTime getProdConfirmedAt() {
        return prodConfirmedAt;
    }

    public void setProdConfirmedAt(LocalDateTime prodConfirmedAt) {
        this.prodConfirmedAt = prodConfirmedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<AttachmentResponse> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentResponse> attachments) {
        this.attachments = attachments;
    }
}
