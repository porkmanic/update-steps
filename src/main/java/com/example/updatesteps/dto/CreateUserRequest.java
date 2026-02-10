package com.example.updatesteps.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CreateUserRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    private String name; // 用户姓名（可选）

    private String role; // 角色：ADMIN 或 DEVELOPER（可选，默认DEVELOPER）
}
