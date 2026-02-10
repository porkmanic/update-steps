package com.example.updatesteps.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StepResponse {
    private Long id;
    private Long versionId;
    private String content;
    private String username;
    private String userDisplayName; // 用户显示名称
    private Long userId;
    private Boolean locked;
    private Boolean afterUat;
    private Boolean uatConfirmed; // UAT已执行确认
    private LocalDateTime uatConfirmedAt;
    private Boolean prodConfirmed; // 生产已执行确认
    private LocalDateTime prodConfirmedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AttachmentResponse> attachments;
}
