package io.github.lb357.hospital_tp_miit.dto;
import java.time.LocalDateTime;
public record AppointmentResponse(Integer appointmentId, PatientResponse patient, DoctorResponse doctor, Integer checkupId, String room, LocalDateTime appointedTime, Boolean isCompleted){}
