
package io.github.lb357.hospital_tp_miit.controller.api;

import io.github.lb357.hospital_tp_miit.component.CookieTokenResolver;
import io.github.lb357.hospital_tp_miit.dto.*;
import io.github.lb357.hospital_tp_miit.model.checkup.Checkup;
import io.github.lb357.hospital_tp_miit.model.session.Session;
import io.github.lb357.hospital_tp_miit.model.specialization.Specialization;
import io.github.lb357.hospital_tp_miit.model.user.*;
import io.github.lb357.hospital_tp_miit.service.AccountService;
import io.github.lb357.hospital_tp_miit.service.AppointmentService;
import io.github.lb357.hospital_tp_miit.service.CheckupService;
import io.github.lb357.hospital_tp_miit.service.SpecializationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Transactional
public class UserController {

    private final CookieTokenResolver cookieTokenResolver;
    private final AccountService accountService;
    private final SpecializationService specializationService;
    private final AppointmentService appointmentService;
    private final CheckupService checkupService;

    @PostMapping("/delete")
    public ResponseEntity<SuccessResponse> delete(@RequestBody UserDeleteRequest request) {
        User administrator = accountService.getUser(cookieTokenResolver.getSession());
        if (administrator == null || administrator.getRole() != UserRole.ADMIN)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        if (request == null || request.userId() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        User target = accountService.getUser(request.userId());
        if (target == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        accountService.deleteUser(target);
        return ResponseEntity.ok(new SuccessResponse(true));
    }

    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody UserUpdateRequest request) {
        User administrator = accountService.getUser(cookieTokenResolver.getSession());
        if (administrator == null || administrator.getRole() != UserRole.ADMIN)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        if (request == null || request.userId() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        User user = accountService.getUser(request.userId());
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        accountService.updateUser(
                user,
                request.lastName().isPresent()?request.lastName().get():null,
                request.firstName().isPresent()?request.firstName().get():null,
                request.patronymicName().isPresent()?request.patronymicName().get():null
        );

        if (user.getRole()==UserRole.PATIENT) {
            Patient patient = accountService.getPatient(user.getId());
            return ResponseEntity.ok(new PatientResponse(
                    new UserResponse(user.getId(), user.getLastName(),
                            user.getFirstName(), user.getPatronymicName()),
                    patient.isGender()
            ));
        }


        if (user.getRole()==UserRole.DOCTOR) {
            Doctor doctor = accountService.getDoctor(user.getId());
            Specialization specialization = doctor.getSpecialization();

            return ResponseEntity.ok(new DoctorResponse(
                    new UserResponse(user.getId(), user.getLastName(),
                            user.getFirstName(), user.getPatronymicName()),
                    specialization == null ? null :
                            new SpecializationResponse(specialization.getId(),
                                    specialization.getName())
            ));
        }

        if (user.getRole()==UserRole.ADMIN)
            return ResponseEntity.ok(new AdminResponse(
                    new UserResponse(user.getId(), user.getLastName(),
                            user.getFirstName(), user.getPatronymicName())
            ));

        return ResponseEntity.ok(new UserResponse(user.getId(),
                user.getLastName(), user.getFirstName(), user.getPatronymicName()));
    }

    @PostMapping("/profile")
    public ResponseEntity<?> profile() {
        Session session = cookieTokenResolver.getSession();
        User user = accountService.getUser(session);

        switch (user) {
            case null -> {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            case Patient patient -> {
                return ResponseEntity.ok(new PatientResponse(
                        new UserResponse(user.getId(), user.getLastName(),
                                user.getFirstName(), user.getPatronymicName()),
                        patient.isGender()
                ));
            }
            case Doctor doctor -> {
                Specialization specialization = doctor.getSpecialization();

                return ResponseEntity.ok(new DoctorResponse(
                        new UserResponse(user.getId(), user.getLastName(),
                                user.getFirstName(), user.getPatronymicName()),
                        specialization == null ? null :
                                new SpecializationResponse(specialization.getId(),
                                        specialization.getName())
                ));
            }
            case Admin admin -> {
                return ResponseEntity.ok(new AdminResponse(
                        new UserResponse(user.getId(), user.getLastName(),
                                user.getFirstName(), user.getPatronymicName())
                ));
            }
            default -> {
            }
        }
        return ResponseEntity.ok(new UserResponse(user.getId(),
                user.getLastName(), user.getFirstName(), user.getPatronymicName()));
    }

    @PostMapping("/get")
    public ResponseEntity<?> get() {
        User administrator = accountService.getUser(cookieTokenResolver.getSession());
        if (administrator == null || administrator.getRole() != UserRole.ADMIN)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        return ResponseEntity.ok(accountService.getAllUsers().stream()
                .map(user -> {
                    if (user.getRole()==UserRole.PATIENT) {
                        Patient patient = accountService.getPatient(user.getId());
                        return (Object) new PatientResponse(
                                new UserResponse(user.getId(), user.getLastName(),
                                        user.getFirstName(), user.getPatronymicName()),
                                patient.isGender()
                        );
                    }
                    if (user.getRole()==UserRole.DOCTOR) {
                        Doctor doctor = accountService.getDoctor(user.getId());
                        Specialization specialization = doctor.getSpecialization();

                        return new DoctorResponse(
                                new UserResponse(user.getId(), user.getLastName(),
                                        user.getFirstName(), user.getPatronymicName()),
                                specialization == null ? null :
                                        new SpecializationResponse(specialization.getId(),
                                                specialization.getName())
                        );
                    }
                    if (user.getRole()==UserRole.ADMIN)
                        return new AdminResponse(
                                new UserResponse(user.getId(), user.getLastName(),
                                        user.getFirstName(), user.getPatronymicName())
                        );

                    return new UserResponse(user.getId(), user.getLastName(),
                            user.getFirstName(), user.getPatronymicName());
                })
                .toList());
    }

    @PostMapping("/specialization/create")
    public ResponseEntity<SpecializationResponse> specializationCreate(
            @RequestBody SpecializationCreateRequest request) {
        User administrator = accountService.getUser(cookieTokenResolver.getSession());
        if (administrator == null || administrator.getRole() != UserRole.ADMIN)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (request == null || request.name() == null || request.name().isBlank())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        String name = request.name().trim();
        boolean alreadyExists = specializationService.getSpecializations().stream()
                .anyMatch(specialization ->
                        specialization.getName().equalsIgnoreCase(name));
        if (alreadyExists)
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        Specialization specialization = specializationService.createSpecialization(name);
        return ResponseEntity.ok(new SpecializationResponse(
                specialization.getId(), specialization.getName()));
    }

    @PostMapping("/specialization/update")
    public ResponseEntity<SuccessResponse> specializationUpdate(
            @RequestBody SpecializationUpdateRequest request) {
        User administrator = accountService.getUser(cookieTokenResolver.getSession());
        if (administrator == null || administrator.getRole() != UserRole.ADMIN)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (request == null || request.specializationId() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        Specialization specialization =
                specializationService.getSpecialization(request.specializationId());
        if (specialization == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        boolean usedByDoctor =
                !accountService.getDoctorsBySpecialization(specialization).isEmpty();
        boolean usedByCheckup = checkupService.getAllCheckups().stream()
                .map(Checkup::getSpecializations)
                .anyMatch(specializations -> specializations.stream()
                        .anyMatch(item ->
                                item.getId().equals(specialization.getId())));
        if (usedByDoctor || usedByCheckup)
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        specializationService.deleteSpecialization(specialization);
        return ResponseEntity.ok(new SuccessResponse(true));
    }

    @PostMapping("/specialization/get")
    public ResponseEntity<List<SpecializationResponse>> specializationGet() {
        User administrator = accountService.getUser(cookieTokenResolver.getSession());
        if (administrator == null || administrator.getRole() != UserRole.ADMIN)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        return ResponseEntity.ok(specializationService.getSpecializations().stream()
                .map(specialization -> new SpecializationResponse(
                        specialization.getId(), specialization.getName()))
                .toList());
    }

    @PostMapping("/specialists")
    public ResponseEntity<List<DoctorResponse>> specialists(@RequestBody SpecialistsRequest request) {
        Session session = cookieTokenResolver.getSession();
        if (accountService.getUser(session) == null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (request == null || request.specializationId() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        Specialization specialization =
                specializationService.getSpecialization(request.specializationId());
        if (specialization == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(accountService.getDoctorsBySpecialization(specialization)
                .stream()
                .map(doctor -> {
                    Specialization doctorSpecialization = doctor.getSpecialization();

                    return new DoctorResponse(
                            new UserResponse(doctor.getId(), doctor.getLastName(),
                                    doctor.getFirstName(), doctor.getPatronymicName()),
                            doctorSpecialization == null ? null :
                                    new SpecializationResponse(
                                            doctorSpecialization.getId(),
                                            doctorSpecialization.getName())
                    );
                })
                .toList());
    }
}
