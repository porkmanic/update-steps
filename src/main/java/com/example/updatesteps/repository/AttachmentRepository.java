package com.example.updatesteps.repository;

import com.example.updatesteps.entity.Attachment;
import com.example.updatesteps.entity.Step;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByStep(Step step);
}
