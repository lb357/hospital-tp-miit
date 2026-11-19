package io.github.lb357.hospital_tp_miit.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
public record PatientResponse(UserResponse user, Boolean gender) {@JsonProperty("role") public String role() {return "patient";}}