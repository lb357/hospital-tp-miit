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
    public ResponseEntity<?> delete(@RequestBody UserDeleteRequest request) {
        if (!isAdministrator()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (request == null || request.userId() == null)
            return ResponseEntity.badRequest().body(new MessageResponse("Не указан идентификатор пользователя"));

        User target = accountService.getUser(request.userId());
        if (target == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Пользователь не найден"));

        if (target instanceof Patient patient) {
            if (!appointmentService.getPatientAppointments(patient).isEmpty())
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new MessageResponse("Нельзя удалить пациента с активными приёмами"));
            if (!checkupService.getPatientCheckups(patient).isEmpty())
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new MessageResponse("Сначала удалите назначенные пациенту медосмотры"));
        }

        if (target instanceof Doctor doctor && !appointmentService.getDoctorAppointments(doctor).isEmpty())
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponse("Нельзя удалить врача, пока в его расписании есть приёмы или свободные слоты"));

        accountService.deleteUser(target);
        return ResponseEntity.ok(new SuccessResponse(true));
    }

    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody UserUpdateRequest request) {
        if (!isAdministrator()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (request == null || request.userId() == null)
            return ResponseEntity.badRequest().body(new MessageResponse("Не указан идентификатор пользователя"));

        User user = accountService.getUser(request.userId());
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Пользователь не найден"));
        if (isBlank(request.lastName()) && isBlank(request.firstName()) && isBlank(request.patronymicName()))
            return ResponseEntity.badRequest().body(new MessageResponse("Укажите хотя бы одно поле для изменения"));

        accountService.updateUser(user, request.lastName(), request.firstName(), request.patronymicName());
        return ResponseEntity.ok(toUserResponse(user));
    }

    @PostMapping("/profile")
    public ResponseEntity<?> profile() {
        Session session = cookieTokenResolver.getSession();
        User user = accountService.getUser(session);
        if (user == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(toUserResponse(user));
    }

    @PostMapping("/get")
    public ResponseEntity<?> get() {
        if (!isAdministrator()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(accountService.getAllUsers().stream().map(this::toUserResponse).toList());
    }

    @PostMapping("/specialization/create")
    public ResponseEntity<?> specializationCreate(@RequestBody SpecializationCreateRequest request) {
        if (!isAdministrator()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (request == null || isBlank(request.name()))
            return ResponseEntity.badRequest().body(new MessageResponse("Введите название специальности"));

        String name = request.name().trim();
        boolean alreadyExists = specializationService.getSpecializations().stream()
                .anyMatch(specialization -> specialization.getName().equalsIgnoreCase(name));
        if (alreadyExists)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse("Такая специальность уже существует"));

        Specialization specialization = specializationService.createSpecialization(name);
        return ResponseEntity.ok(toSpecializationResponse(specialization));
    }

    // The UML uses the path name "update", but describes this operation as deleting a specialization.
    @PostMapping("/specialization/update")
    public ResponseEntity<?> specializationUpdate(@RequestBody SpecializationUpdateRequest request) {
        if (!isAdministrator()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (request == null || request.specializationId() == null)
            return ResponseEntity.badRequest().body(new MessageResponse("Не указан идентификатор специальности"));

        Specialization specialization = specializationService.getSpecialization(request.specializationId());
        if (specialization == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Специальность не найдена"));

        boolean usedByDoctor = !accountService.getDoctorsBySpecialization(specialization).isEmpty();
        boolean usedByCheckup = checkupService.getAllCheckups().stream()
                .map(Checkup::getSpecializations)
                .anyMatch(specializations -> specializations.stream()
                        .anyMatch(item -> item.getId().equals(specialization.getId())));
        if (usedByDoctor || usedByCheckup)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponse("Нельзя удалить специальность, которая используется врачом или медосмотром"));

        specializationService.deleteSpecialization(specialization);
        return ResponseEntity.ok(new SuccessResponse(true));
    }

    @PostMapping("/specialization/get")
    public ResponseEntity<?> specializationGet() {
        if (!isAdministrator()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(specializationService.getSpecializations().stream()
                .map(this::toSpecializationResponse).toList());
    }

    @PostMapping("/specialists")
    public ResponseEntity<?> specialists(@RequestBody SpecialistsRequest request) {
        Session session = cookieTokenResolver.getSession();
        if (accountService.getUser(session) == null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (request == null || request.specializationId() == null)
            return ResponseEntity.badRequest().body(new MessageResponse("Не указан идентификатор специальности"));

        Specialization specialization = specializationService.getSpecialization(request.specializationId());
        if (specialization == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Специальность не найдена"));

        return ResponseEntity.ok(accountService.getDoctorsBySpecialization(specialization).stream()
                .map(this::toDoctorResponse).toList());
    }

    private boolean isAdministrator() {
        User user = accountService.getUser(cookieTokenResolver.getSession());
        return user != null && user.getRole() == UserRole.ADMIN;
    }

    private Object toUserResponse(User user) {
        if (user instanceof Patient patient)
            return new PatientResponse(toBasicUserResponse(patient), patient.isGender());
        if (user instanceof Doctor doctor) return toDoctorResponse(doctor);
        if (user instanceof Admin) return new AdminResponse(toBasicUserResponse(user));
        return toBasicUserResponse(user);
    }

    private DoctorResponse toDoctorResponse(Doctor doctor) {
        Specialization specialization = doctor.getSpecialization();
        return new DoctorResponse(
                toBasicUserResponse(doctor),
                specialization == null ? null : toSpecializationResponse(specialization)
        );
    }

    private UserResponse toBasicUserResponse(User user) {
        return new UserResponse(user.getId(), user.getLastName(), user.getFirstName(), user.getPatronymicName());
    }

    private SpecializationResponse toSpecializationResponse(Specialization specialization) {
        return new SpecializationResponse(specialization.getId(), specialization.getName());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
