package com.example.demo.dto;

public class JwtResponse {
    private Long id;
    private String token;
    private String type = "Bearer";
    private String username;
    private String role;
    private String displayName;
    private String email;
    private String profileImagePath;

    public JwtResponse(Long id, String accessToken, String username, String role, String displayName, String email, String profileImagePath) {
        this.id = id;
        this.token = accessToken;
        this.username = username;
        this.role = role;
        this.displayName = displayName;
        this.email = email;
        this.profileImagePath = profileImagePath;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getProfileImagePath() { return profileImagePath; }
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }
}
