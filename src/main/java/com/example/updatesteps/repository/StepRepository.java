package com.example.updatesteps.repository;

import com.example.updatesteps.entity.Step;
import com.example.updatesteps.entity.Version;
import com.example.updatesteps.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StepRepository extends JpaRepository<Step, Long> {
    List<Step> findByVersionOrderByCreatedAtDesc(Version version);

    // 按创建时间升序排序
    List<Step> findByVersionOrderByCreatedAtAsc(Version version);

    List<Step> findByVersionAndAfterUat(Version version, Boolean afterUat);

    List<Step> findByUserOrderByCreatedAtDesc(User user);
}
