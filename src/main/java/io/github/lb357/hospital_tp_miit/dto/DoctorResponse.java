package io.github.lb357.hospital_tp_miit.dto;

public record DoctorResponse(UserResponse user, SpecializationResponse specialization) {
    static final String role = "doctor";
}
