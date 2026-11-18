package io.github.lb357.hospital_tp_miit.dto;
import java.util.List;
public record CheckupResponse(Integer checkupId, PatientResponse patient, List<SpecializationResponse> specializationIds) {}
