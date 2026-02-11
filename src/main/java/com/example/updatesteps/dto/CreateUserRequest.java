package com.example.updatesteps.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateUserRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    private String name; // 用户姓名（可选）

    private String role; // 角色：ADMIN 或 DEVELOPER（可选，默认DEVELOPER）

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
