package io.github.lb357.hospital_tp_miit.controller.api;

import io.github.lb357.hospital_tp_miit.component.CookieTokenResolver;
import io.github.lb357.hospital_tp_miit.dto.*;
import io.github.lb357.hospital_tp_miit.model.appointment.Appointment;
import io.github.lb357.hospital_tp_miit.model.checkup.Checkup;
import io.github.lb357.hospital_tp_miit.model.session.Session;
import io.github.lb357.hospital_tp_miit.model.specialization.Specialization;
import io.github.lb357.hospital_tp_miit.model.user.Patient;
import io.github.lb357.hospital_tp_miit.model.user.User;
import io.github.lb357.hospital_tp_miit.model.user.UserRole;
import io.github.lb357.hospital_tp_miit.service.AccountService;
import io.github.lb357.hospital_tp_miit.service.AppointmentService;
import io.github.lb357.hospital_tp_miit.service.CheckupService;
import io.github.lb357.hospital_tp_miit.service.SpecializationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkup")
@RequiredArgsConstructor
@Transactional
public class CheckupController {
    private final CookieTokenResolver cookieTokenResolver;
    private final AccountService accountService;
    private final CheckupService checkupService;
    private final SpecializationService specializationService;
    private final AppointmentService appointmentService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CheckupCreateRequest request) {
        Session session = cookieTokenResolver.getSession();
        User currentUser = accountService.getUser(session);
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        if (request == null || request.patientId() == null
                || request.specializationIds() == null || request.specializationIds().isEmpty())
            return ResponseEntity.badRequest().body(new MessageResponse("Укажите пациента и хотя бы одну специальность"));

        Patient patient = accountService.getPatient(request.patientId());
        if (patient == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Пациент не найден"));

        Map<Integer, Specialization> specializationsById = new LinkedHashMap<>();
        for (Integer specializationId : request.specializationIds()) {
            if (specializationId == null)
                return ResponseEntity.badRequest().body(new MessageResponse("Идентификатор специальности не может быть пустым"));
            Specialization specialization = specializationService.getSpecialization(specializationId);
            if (specialization == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Специальность с идентификатором " + specializationId + " не найдена"));
            specializationsById.put(specializationId, specialization);
        }

        Checkup checkup = checkupService.createCheckup(patient, List.copyOf(specializationsById.values()));
        return ResponseEntity.ok(toResponse(checkup));
    }

    @PostMapping("/delete")
    public ResponseEntity<?> delete(@RequestBody CheckupDeleteRequest request) {
        Session session = cookieTokenResolver.getSession();
        User currentUser = accountService.getUser(session);
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (request == null || request.checkupId() == null)
            return ResponseEntity.badRequest().body(new MessageResponse("Не указан идентификатор медосмотра"));

        Checkup checkup = checkupService.getCheckup(request.checkupId());
        if (checkup == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Назначение медосмотра не найдено"));

        // A checkup referenced by appointments is part of their history and must not be orphaned.
        boolean hasAppointments = appointmentService.getAllAppointments().stream()
                .map(Appointment::getCheckup)
                .anyMatch(appointmentCheckup -> appointmentCheckup != null
                        && appointmentCheckup.getId().equals(checkup.getId()));
        if (hasAppointments)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponse("Нельзя удалить медосмотр, связанный с приёмами"));

        checkupService.deleteCheckup(checkup);
        return ResponseEntity.ok(new SuccessResponse(true));
    }

    @PostMapping("/get")
    public ResponseEntity<?> get(@RequestBody(required = false) CheckupGetRequest request) {
        Session session = cookieTokenResolver.getSession();
        User currentUser = accountService.getUser(session);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        List<Checkup> checkups;
        if (currentUser.getRole() == UserRole.ADMIN) {
            if (request != null && request.patientId() != null) {
                Patient patient = accountService.getPatient(request.patientId());
                if (patient == null)
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Пациент не найден"));
                checkups = checkupService.getPatientCheckups(patient);
            } else {
                checkups = checkupService.getAllCheckups();
            }
        } else if (currentUser.getRole() == UserRole.PATIENT) {
            if (request != null && request.patientId() != null
                    && !request.patientId().equals(currentUser.getId()))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            checkups = checkupService.getPatientCheckups(accountService.getPatient(currentUser.getId()));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(checkups.stream().map(this::toResponse).toList());
    }

    private CheckupResponse toResponse(Checkup checkup) {
        Patient patient = checkup.getPatient();
        return new CheckupResponse(
                checkup.getId(),
                new PatientResponse(
                        new UserResponse(patient.getId(), patient.getLastName(), patient.getFirstName(), patient.getPatronymicName()),
                        patient.isGender()
                ),
                checkup.getSpecializations().stream()
                        .map(specialization -> new SpecializationResponse(specialization.getId(), specialization.getName()))
                        .toList()
        );
    }
}
