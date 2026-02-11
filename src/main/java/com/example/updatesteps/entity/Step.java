package com.example.updatesteps.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "steps")
public class Step {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private Version version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Boolean locked = false;

    @Column(name = "after_uat", nullable = false)
    private Boolean afterUat = false;

    // 新增：UAT已执行确认
    @Column(name = "uat_confirmed", nullable = false)
    private Boolean uatConfirmed = false;

    @Column(name = "uat_confirmed_at")
    private LocalDateTime uatConfirmedAt;

    // 新增：生产已执行确认
    @Column(name = "prod_confirmed", nullable = false)
    private Boolean prodConfirmed = false;

    @Column(name = "prod_confirmed_at")
    private LocalDateTime prodConfirmedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "step", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments;

    public Step() {
    }

    public Step(Long id, Version version, User user, String content, Boolean locked, Boolean afterUat,
            Boolean uatConfirmed, LocalDateTime uatConfirmedAt, Boolean prodConfirmed, LocalDateTime prodConfirmedAt,
            LocalDateTime createdAt, LocalDateTime updatedAt, List<Attachment> attachments) {
        this.id = id;
        this.version = version;
        this.user = user;
        this.content = content;
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

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
