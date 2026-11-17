package io.github.lb357.hospital_tp_miit.dto;

public record PatientResponse(UserResponse user, Boolean gender) {
    static final String role = "patient";
}