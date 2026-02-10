package com.example.updatesteps.service;

import com.example.updatesteps.dto.*;
import com.example.updatesteps.entity.*;
import com.example.updatesteps.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StepService {

    private final StepRepository stepRepository;
    private final VersionRepository versionRepository;
    private final AttachmentRepository attachmentRepository;

    @Value("${app.upload.path}")
    private String uploadPath;

    public StepService(StepRepository stepRepository, VersionRepository versionRepository,
            AttachmentRepository attachmentRepository) {
        this.stepRepository = stepRepository;
        this.versionRepository = versionRepository;
        this.attachmentRepository = attachmentRepository;
    }

    public StepResponse createStep(User user, CreateStepRequest request) {
        Version version = versionRepository.findById(request.getVersionId())
                .orElseThrow(() -> new RuntimeException("版本不存在"));

        if (version.getStatus() == Version.Status.ARCHIVED) {
            throw new RuntimeException("已归档版本不能添加步骤");
        }

        Step step = new Step();
        step.setVersion(version);
        step.setUser(user);
        step.setContent(request.getContent());
        step.setLocked(false);
        step.setAfterUat(version.getStatus() == Version.Status.UAT);
        step.setUatConfirmed(false);
        step.setProdConfirmed(false);
        step.setAttachments(new ArrayList<>());
        stepRepository.save(step);

        return toStepResponse(step);
    }

    public StepResponse updateStep(User user, Long id, UpdateStepRequest request) {
        Step step = stepRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("步骤不存在"));

        // 检查权限：只有创建者可以编辑自己的步骤
        if (!step.getUser().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("无权编辑此步骤");
        }

        // 检查锁定状态或UAT确认状态
        if (step.getLocked() || Boolean.TRUE.equals(step.getUatConfirmed())) {
            throw new RuntimeException("UAT执行后的步骤不能修改");
        }

        // 检查版本状态
        if (step.getVersion().getStatus() == Version.Status.ARCHIVED) {
            throw new RuntimeException("已归档版本的步骤不能修改");
        }

        step.setContent(request.getContent());
        stepRepository.save(step);

        return toStepResponse(step);
    }

    public void deleteStep(User user, Long id) {
        Step step = stepRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("步骤不存在"));

        // 检查权限
        if (!step.getUser().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("无权删除此步骤");
        }

        // 检查锁定状态或UAT确认状态
        if (step.getLocked() || Boolean.TRUE.equals(step.getUatConfirmed())) {
            throw new RuntimeException("UAT执行后的步骤不能删除");
        }

        // 检查版本状态
        if (step.getVersion().getStatus() == Version.Status.ARCHIVED) {
            throw new RuntimeException("已归档版本的步骤不能删除");
        }

        // 删除附件文件
        for (Attachment attachment : step.getAttachments()) {
            try {
                Files.deleteIfExists(Paths.get(attachment.getFilepath()));
            } catch (IOException e) {
                // 忽略删除失败
            }
        }

        stepRepository.delete(step);
    }

    public List<StepResponse> getStepsByVersion(Long versionId) {
        Version version = versionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("版本不存在"));

        // 按创建时间升序排序
        return stepRepository.findByVersionOrderByCreatedAtAsc(version).stream()
                .map(this::toStepResponse)
                .collect(Collectors.toList());
    }

    // 确认UAT已执行（仅管理员）
    public StepResponse confirmUat(Long id) {
        Step step = stepRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("步骤不存在"));

        step.setUatConfirmed(true);
        step.setUatConfirmedAt(LocalDateTime.now());
        stepRepository.save(step);

        return toStepResponse(step);
    }

    // 取消UAT确认（仅管理员）
    public StepResponse cancelUatConfirm(Long id) {
        Step step = stepRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("步骤不存在"));

        step.setUatConfirmed(false);
        step.setUatConfirmedAt(null);
        stepRepository.save(step);

        return toStepResponse(step);
    }

    // 确认生产已执行（仅管理员）
    public StepResponse confirmProd(Long id) {
        Step step = stepRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("步骤不存在"));

        step.setProdConfirmed(true);
        step.setProdConfirmedAt(LocalDateTime.now());
        stepRepository.save(step);

        return toStepResponse(step);
    }

    // 取消生产确认（仅管理员）
    public StepResponse cancelProdConfirm(Long id) {
        Step step = stepRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("步骤不存在"));

        step.setProdConfirmed(false);
        step.setProdConfirmedAt(null);
        stepRepository.save(step);

        return toStepResponse(step);
    }

    @Transactional
    public AttachmentResponse uploadAttachment(User user, Long stepId, MultipartFile file) throws IOException {
        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("步骤不存在"));

        // 检查权限
        if (!step.getUser().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("无权上传附件");
        }

        // 检查锁定状态
        if (step.getLocked()) {
            throw new RuntimeException("UAT执行后的步骤不能添加附件");
        }

        // 确保上传目录存在
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadDir.resolve(newFilename);

        // 保存文件
        Files.copy(file.getInputStream(), filePath);

        // 保存附件记录
        Attachment attachment = new Attachment();
        attachment.setStep(step);
        attachment.setFilename(originalFilename);
        attachment.setFilepath(filePath.toString());
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachmentRepository.save(attachment);

        return toAttachmentResponse(attachment);
    }

    public void deleteAttachment(User user, Long attachmentId) throws IOException {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("附件不存在"));

        Step step = attachment.getStep();

        // 检查权限
        if (!step.getUser().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("无权删除附件");
        }

        // 检查锁定状态
        if (step.getLocked()) {
            throw new RuntimeException("UAT执行后的步骤不能删除附件");
        }

        // 删除文件
        Files.deleteIfExists(Paths.get(attachment.getFilepath()));

        // 删除记录
        attachmentRepository.delete(attachment);
    }

    private StepResponse toStepResponse(Step step) {
        List<AttachmentResponse> attachments = step.getAttachments() != null
                ? step.getAttachments().stream().map(this::toAttachmentResponse).collect(Collectors.toList())
                : new ArrayList<>();

        return new StepResponse(
                step.getId(),
                step.getVersion().getId(),
                step.getContent(),
                step.getUser().getUsername(),
                step.getUser().getDisplayName(),
                step.getUser().getId(),
                step.getLocked(),
                step.getAfterUat(),
                step.getUatConfirmed(),
                step.getUatConfirmedAt(),
                step.getProdConfirmed(),
                step.getProdConfirmedAt(),
                step.getCreatedAt(),
                step.getUpdatedAt(),
                attachments);
    }

    private AttachmentResponse toAttachmentResponse(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getFilename(),
                "/uploads/" + Paths.get(attachment.getFilepath()).getFileName().toString(),
                attachment.getFileType(),
                attachment.getFileSize(),
                attachment.getCreatedAt());
    }
}
