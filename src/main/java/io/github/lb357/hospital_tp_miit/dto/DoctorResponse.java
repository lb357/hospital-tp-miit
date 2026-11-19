package io.github.lb357.hospital_tp_miit.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
public record DoctorResponse(UserResponse user, SpecializationResponse specialization) {@JsonProperty("role") public String role() {return "doctor";}}