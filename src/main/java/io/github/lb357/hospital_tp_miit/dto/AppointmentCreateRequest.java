package io.github.lb357.hospital_tp_miit.dto;
import java.time.LocalDateTime;
public record AppointmentCreateRequest(Integer doctorId, String room, LocalDateTime appointedTime){}
