package io.github.lb357.hospital_tp_miit.dto;

import io.github.lb357.hospital_tp_miit.model.user.UserRole;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Setter;

public record PatientResponse(UserResponse user, Boolean gender) {
    static final String role = "patient";
}