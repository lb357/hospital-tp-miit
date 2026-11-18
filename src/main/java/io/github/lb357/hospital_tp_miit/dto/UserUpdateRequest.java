package io.github.lb357.hospital_tp_miit.dto;

import java.util.Optional;

public record UserUpdateRequest(
        Integer userId,
        Optional<String> lastName,
        Optional<String> firstName,
        Optional<String> patronymicName
) {}
