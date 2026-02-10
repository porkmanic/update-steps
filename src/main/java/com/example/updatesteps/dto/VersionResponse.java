package com.example.updatesteps.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VersionResponse {
    private Long id;
    private String versionNumber;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime uatAt;
    private LocalDateTime archivedAt;
    private int stepCount;
    private int newStepCount; // UAT后新增步骤数
}
