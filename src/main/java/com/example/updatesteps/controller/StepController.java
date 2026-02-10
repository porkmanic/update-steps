package com.example.updatesteps.controller;

import com.example.updatesteps.dto.*;
import com.example.updatesteps.entity.User;
import com.example.updatesteps.service.StepService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/steps")
public class StepController {

    private final StepService stepService;

    public StepController(StepService stepService) {
        this.stepService = stepService;
    }

    @GetMapping("/version/{versionId}")
    public ResponseEntity<ApiResponse<List<StepResponse>>> getStepsByVersion(@PathVariable Long versionId) {
        try {
            List<StepResponse> steps = stepService.getStepsByVersion(versionId);
            return ResponseEntity.ok(ApiResponse.success(steps));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StepResponse>> createStep(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateStepRequest request) {
        try {
            StepResponse response = stepService.createStep(user, request);
            return ResponseEntity.ok(ApiResponse.success("步骤添加成功", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StepResponse>> updateStep(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateStepRequest request) {
        try {
            StepResponse response = stepService.updateStep(user, id, request);
            return ResponseEntity.ok(ApiResponse.success("步骤更新成功", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStep(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        try {
            stepService.deleteStep(user, id);
            return ResponseEntity.ok(ApiResponse.success("步骤删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // UAT确认 - 仅管理员
    @PostMapping("/{id}/confirm-uat")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StepResponse>> confirmUat(@PathVariable Long id) {
        try {
            StepResponse response = stepService.confirmUat(id);
            return ResponseEntity.ok(ApiResponse.success("UAT执行已确认", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 取消UAT确认 - 仅管理员
    @PostMapping("/{id}/cancel-uat-confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StepResponse>> cancelUatConfirm(@PathVariable Long id) {
        try {
            StepResponse response = stepService.cancelUatConfirm(id);
            return ResponseEntity.ok(ApiResponse.success("UAT确认已取消", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 生产确认 - 仅管理员
    @PostMapping("/{id}/confirm-prod")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StepResponse>> confirmProd(@PathVariable Long id) {
        try {
            StepResponse response = stepService.confirmProd(id);
            return ResponseEntity.ok(ApiResponse.success("生产执行已确认", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 取消生产确认 - 仅管理员
    @PostMapping("/{id}/cancel-prod-confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StepResponse>> cancelProdConfirm(@PathVariable Long id) {
        try {
            StepResponse response = stepService.cancelProdConfirm(id);
            return ResponseEntity.ok(ApiResponse.success("生产确认已取消", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{stepId}/attachments")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadAttachment(
            @AuthenticationPrincipal User user,
            @PathVariable Long stepId,
            @RequestParam("file") MultipartFile file) {
        try {
            AttachmentResponse response = stepService.uploadAttachment(user, stepId, file);
            return ResponseEntity.ok(ApiResponse.success("附件上传成功", response));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("文件上传失败: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @AuthenticationPrincipal User user,
            @PathVariable Long attachmentId) {
        try {
            stepService.deleteAttachment(user, attachmentId);
            return ResponseEntity.ok(ApiResponse.success("附件删除成功", null));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("文件删除失败: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
