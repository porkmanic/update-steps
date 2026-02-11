package com.example.updatesteps.dto;

public class LoginResponse {
    private String token;
    private Long id;
    private String username;
    private String name;
    private String displayName;
    private String role;

    public LoginResponse() {
    }

    public LoginResponse(String token, Long id, String username, String name, String displayName, String role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.name = name;
        this.displayName = displayName;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
