package com.example.updatesteps.repository;

import com.example.updatesteps.entity.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {
    List<Version> findByStatusNot(Version.Status status);

    List<Version> findByStatus(Version.Status status);

    // 历史版本按创建时间倒序排序
    List<Version> findByStatusOrderByCreatedAtDesc(Version.Status status);

    Optional<Version> findByVersionNumber(String versionNumber);

    boolean existsByVersionNumber(String versionNumber);
}
