package io.github.lb357.hospital_tp_miit.dto;
import java.time.LocalDateTime;
public record EntryResponse(Integer appointmentId, LocalDateTime appointedTime, DoctorResponse doctor, String content) {}
