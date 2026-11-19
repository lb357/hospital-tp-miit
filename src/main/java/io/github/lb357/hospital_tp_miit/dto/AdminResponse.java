package io.github.lb357.hospital_tp_miit.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
public record AdminResponse(UserResponse user) {@JsonProperty("role") public String role() {return "admin";}}
