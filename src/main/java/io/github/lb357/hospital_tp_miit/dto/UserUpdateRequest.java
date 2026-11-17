package io.github.lb357.hospital_tp_miit.dto;

public record UserUpdateRequest(
        Integer userId,
        String lastName,
        String firstName,
        String patronymicName
) {}
