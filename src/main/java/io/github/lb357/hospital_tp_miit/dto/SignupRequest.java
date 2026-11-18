package io.github.lb357.hospital_tp_miit.dto;
import java.util.Optional;
public record SignupRequest(String lastName, String firstName, String patronymicName, String password, String role, Optional<Boolean> patientGender, Optional<Integer> doctorSpecializationId) {}
