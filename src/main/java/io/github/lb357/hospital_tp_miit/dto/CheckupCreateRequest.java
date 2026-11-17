package io.github.lb357.hospital_tp_miit.dto;

import java.util.List;

public record CheckupCreateRequest(Integer patientId, List<Integer> specializationIds) {}
