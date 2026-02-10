package com.example.updatesteps.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentResponse {
    private Long id;
    private String filename;
    private String filepath;
    private String fileType;
    private Long fileSize;
    private LocalDateTime createdAt;
}
