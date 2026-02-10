package com.example.updatesteps.service;

import com.example.updatesteps.dto.*;
import com.example.updatesteps.entity.Version;
import com.example.updatesteps.entity.Step;
import com.example.updatesteps.repository.VersionRepository;
import com.example.updatesteps.repository.StepRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VersionService {

    private final VersionRepository versionRepository;
    private final StepRepository stepRepository;

    public VersionService(VersionRepository versionRepository, StepRepository stepRepository) {
        this.versionRepository = versionRepository;
        this.stepRepository = stepRepository;
    }

    public VersionResponse createVersion(CreateVersionRequest request) {
        if (versionRepository.existsByVersionNumber(request.getVersionNumber())) {
            throw new RuntimeException("版本号已存在");
        }

        Version version = new Version();
        version.setVersionNumber(request.getVersionNumber());
        version.setStatus(Version.Status.DEV);
        versionRepository.save(version);

        return toVersionResponse(version);
    }

    public List<VersionResponse> getActiveVersions() {
        return versionRepository.findByStatusNot(Version.Status.ARCHIVED).stream()
                .map(this::toVersionResponse)
                .collect(Collectors.toList());
    }

    public List<VersionResponse> getArchivedVersions() {
        // 按创建时间倒序排序
        return versionRepository.findByStatusOrderByCreatedAtDesc(Version.Status.ARCHIVED).stream()
                .map(this::toVersionResponse)
                .collect(Collectors.toList());
    }

    public VersionResponse getVersion(Long id) {
        Version version = versionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("版本不存在"));
        return toVersionResponse(version);
    }

    @Transactional
    public VersionResponse startUat(Long id) {
        Version version = versionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("版本不存在"));

        if (version.getStatus() != Version.Status.DEV) {
            throw new RuntimeException("只有开发中的版本可以开始UAT");
        }

        // 锁定所有现有步骤
        List<Step> steps = stepRepository.findByVersionOrderByCreatedAtDesc(version);
        for (Step step : steps) {
            step.setLocked(true);
        }
        stepRepository.saveAll(steps);

        version.setStatus(Version.Status.UAT);
        version.setUatAt(LocalDateTime.now());
        versionRepository.save(version);

        return toVersionResponse(version);
    }

    @Transactional
    public VersionResponse cancelUat(Long id) {
        Version version = versionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("版本不存在"));

        if (version.getStatus() != Version.Status.UAT) {
            throw new RuntimeException("只有UAT中的版本可以取消UAT");
        }

        // 解锁所有步骤
        List<Step> steps = stepRepository.findByVersionOrderByCreatedAtDesc(version);
        for (Step step : steps) {
            step.setLocked(false);
            step.setAfterUat(false);
        }
        stepRepository.saveAll(steps);

        version.setStatus(Version.Status.DEV);
        version.setUatAt(null);
        versionRepository.save(version);

        return toVersionResponse(version);
    }

    public VersionResponse archiveVersion(Long id) {
        Version version = versionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("版本不存在"));

        version.setStatus(Version.Status.ARCHIVED);
        version.setArchivedAt(LocalDateTime.now());
        versionRepository.save(version);

        return toVersionResponse(version);
    }

    public VersionResponse unarchiveVersion(Long id) {
        Version version = versionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("版本不存在"));

        if (version.getStatus() != Version.Status.ARCHIVED) {
            throw new RuntimeException("只有已归档版本可以恢复");
        }

        version.setStatus(Version.Status.DEV);
        version.setArchivedAt(null);
        versionRepository.save(version);

        return toVersionResponse(version);
    }

    private VersionResponse toVersionResponse(Version version) {
        List<Step> steps = stepRepository.findByVersionOrderByCreatedAtDesc(version);
        int stepCount = steps.size();
        int newStepCount = (int) steps.stream().filter(Step::getAfterUat).count();

        return new VersionResponse(
                version.getId(),
                version.getVersionNumber(),
                version.getStatus().name(),
                version.getCreatedAt(),
                version.getUatAt(),
                version.getArchivedAt(),
                stepCount,
                newStepCount);
    }
}
