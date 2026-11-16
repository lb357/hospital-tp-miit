package io.github.lb357.hospital_tp_miit.dto;

public record AdminResponse(UserResponse user) {
    static final String role = "admin";
}
