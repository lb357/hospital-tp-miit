package io.github.lb357.hospital_tp_miit.dto;

public record SignupRequest(String lastName, String firstName, String patronymicName, String password, String role, Boolean patientGender, Integer doctorSpecializationId) {}
