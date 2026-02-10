package com.example.updatesteps.controller;

import com.example.updatesteps.dto.*;
import com.example.updatesteps.service.VersionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/versions")
public class VersionController {

    private final VersionService versionService;

    public VersionController(VersionService versionService) {
        this.versionService = versionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VersionResponse>>> getActiveVersions() {
        List<VersionResponse> versions = versionService.getActiveVersions();
        return ResponseEntity.ok(ApiResponse.success(versions));
    }

    @GetMapping("/archived")
    public ResponseEntity<ApiResponse<List<VersionResponse>>> getArchivedVersions() {
        List<VersionResponse> versions = versionService.getArchivedVersions();
        return ResponseEntity.ok(ApiResponse.success(versions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VersionResponse>> getVersion(@PathVariable Long id) {
        try {
            VersionResponse version = versionService.getVersion(id);
            return ResponseEntity.ok(ApiResponse.success(version));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VersionResponse>> createVersion(
            @Valid @RequestBody CreateVersionRequest request) {
        try {
            VersionResponse response = versionService.createVersion(request);
            return ResponseEntity.ok(ApiResponse.success("版本创建成功", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/start-uat")
    public ResponseEntity<ApiResponse<VersionResponse>> startUat(@PathVariable Long id) {
        try {
            VersionResponse response = versionService.startUat(id);
            return ResponseEntity.ok(ApiResponse.success("已开始UAT，现有步骤已锁定", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel-uat")
    public ResponseEntity<ApiResponse<VersionResponse>> cancelUat(@PathVariable Long id) {
        try {
            VersionResponse response = versionService.cancelUat(id);
            return ResponseEntity.ok(ApiResponse.success("已取消UAT，步骤已解锁", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<VersionResponse>> archiveVersion(@PathVariable Long id) {
        try {
            VersionResponse response = versionService.archiveVersion(id);
            return ResponseEntity.ok(ApiResponse.success("版本已归档", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/unarchive")
    public ResponseEntity<ApiResponse<VersionResponse>> unarchiveVersion(@PathVariable Long id) {
        try {
            VersionResponse response = versionService.unarchiveVersion(id);
            return ResponseEntity.ok(ApiResponse.success("版本已恢复", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
