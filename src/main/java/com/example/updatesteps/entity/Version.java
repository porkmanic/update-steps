package com.example.updatesteps.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "versions")
public class Version {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "version_number", unique = true, nullable = false)
    private String versionNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "uat_at")
    private LocalDateTime uatAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    public Version() {
    }

    public Version(Long id, String versionNumber, Status status, LocalDateTime createdAt, LocalDateTime uatAt,
            LocalDateTime archivedAt) {
        this.id = id;
        this.versionNumber = versionNumber;
        this.status = status;
        this.createdAt = createdAt;
        this.uatAt = uatAt;
        this.archivedAt = archivedAt;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = Status.DEV;
        }
    }

    public enum Status {
        DEV,
        UAT,
        ARCHIVED
    }
}
