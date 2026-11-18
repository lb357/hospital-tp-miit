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
    public ResponseEntity<CheckupResponse> create(@RequestBody CheckupCreateRequest request) {
        Session session = cookieTokenResolver.getSession();
        User currentUser = accountService.getUser(session);
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        if (request == null || request.patientId() == null
                || request.specializationIds() == null || request.specializationIds().isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        Patient patient = accountService.getPatient(request.patientId());
        if (patient == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Map<Integer, Specialization> specializationsById = new LinkedHashMap<>();
        for (Integer specializationId : request.specializationIds()) {
            if (specializationId == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            Specialization specialization = specializationService.getSpecialization(specializationId);
            if (specialization == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            specializationsById.put(specializationId, specialization);
        }

        Checkup checkup = checkupService.createCheckup(patient, List.copyOf(specializationsById.values()));
        return ResponseEntity.ok(
                new CheckupResponse(
                        checkup.getId(),
                        new PatientResponse(
                                new UserResponse(patient.getId(), patient.getLastName(), patient.getFirstName(), patient.getPatronymicName()),
                                patient.isGender()
                        ),
                        checkup.getSpecializations().stream()
                                .map(specialization -> new SpecializationResponse(
                                        specialization.getId(), specialization.getName())
                                )
                                .toList()
        ));
    }

    @PostMapping("/delete")
    public ResponseEntity<SuccessResponse> delete(@RequestBody CheckupDeleteRequest request) {
        Session session = cookieTokenResolver.getSession();
        User currentUser = accountService.getUser(session);
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (request == null || request.checkupId() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        Checkup checkup = checkupService.getCheckup(request.checkupId());
        if (checkup == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SuccessResponse(false));
        else {
            checkupService.deleteCheckup(checkup);
            return ResponseEntity.ok(new SuccessResponse(true));
        }
    }

    @PostMapping("/get")
    public ResponseEntity<List<CheckupResponse>> get(@RequestBody CheckupGetRequest request) {
        Session session = cookieTokenResolver.getSession();
        User currentUser = accountService.getUser(session);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        List<Checkup> checkups;
        if (currentUser.getRole() == UserRole.ADMIN) {
            if (request != null && request.patientId().isPresent()) {
                Patient patient = accountService.getPatient(request.patientId().get());
                if (patient == null)
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                checkups = checkupService.getPatientCheckups(patient);
            } else {
                checkups = checkupService.getAllCheckups();
            }
        } else if (currentUser.getRole() == UserRole.PATIENT) {
            if (request != null && request.patientId().isPresent()
                    && !request.patientId().get().equals(currentUser.getId()))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            checkups = checkupService.getPatientCheckups(accountService.getPatient(currentUser.getId()));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(checkups.stream().map(checkup -> {
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
        }).toList());
    }
}
