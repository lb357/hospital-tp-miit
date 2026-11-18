
package io.github.lb357.hospital_tp_miit.controller.api;

import io.github.lb357.hospital_tp_miit.component.CookieTokenResolver;
import io.github.lb357.hospital_tp_miit.dto.*;
import io.github.lb357.hospital_tp_miit.model.session.Session;
import io.github.lb357.hospital_tp_miit.model.specialization.Specialization;
import io.github.lb357.hospital_tp_miit.model.user.*;
import io.github.lb357.hospital_tp_miit.service.AccountService;
import io.github.lb357.hospital_tp_miit.service.SpecializationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CookieTokenResolver cookieTokenResolver;
    private final AccountService accountService;
    private final SpecializationService specializationService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Session session = cookieTokenResolver.getSession();

        if (session != null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        String token = accountService.login(request.userId(), request.password());

        if (token == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        User user = accountService.getSession(token).getUser();

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(30))
                .build();

        if (user.getRole() == UserRole.PATIENT) {
            Patient patient = accountService.getPatient(user.getId());

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new PatientResponse(
                            new UserResponse(user.getId(), user.getLastName(),
                                    user.getFirstName(), user.getPatronymicName()),
                            patient.isGender()
                    ));
        }

        if (user.getRole() == UserRole.DOCTOR) {
            Doctor doctor = accountService.getDoctor(user.getId());
            Specialization specialization = doctor.getSpecialization();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new DoctorResponse(
                            new UserResponse(user.getId(), user.getLastName(),
                                    user.getFirstName(), user.getPatronymicName()),
                            specialization == null ? null :
                                    new SpecializationResponse(
                                            specialization.getId(),
                                            specialization.getName())
                    ));
        }

        if (user.getRole() == UserRole.ADMIN) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AdminResponse(
                            new UserResponse(user.getId(), user.getLastName(),
                                    user.getFirstName(), user.getPatronymicName())
                    ));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse> logout() {
        String token = cookieTokenResolver.resolve();
        Session session = accountService.getSession(token);

        if (session == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        try {
            accountService.logout(token);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new SuccessResponse(true));

        } catch (IllegalArgumentException ignore) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new SuccessResponse(false));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        Session session = cookieTokenResolver.getSession();

        if (session == null ||
                accountService.getUser(session).getRole() != UserRole.ADMIN)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        User user;

        switch (request.role()) {
            case "patient" -> user = accountService.signupPatient(
                    request.lastName(),
                    request.firstName(),
                    request.patronymicName(),
                    request.password(),
                    request.patientGender().isPresent()
                            ? request.patientGender().get() : null
            );

            case "doctor" -> user = accountService.signupDoctor(
                    request.lastName(),
                    request.firstName(),
                    request.patronymicName(),
                    request.password(),
                    specializationService.getSpecialization(
                            request.doctorSpecializationId().isPresent()
                                    ? request.doctorSpecializationId().get() : null
                    )
            );

            case "admin" -> user = accountService.signupAdmin(
                    request.lastName(),
                    request.firstName(),
                    request.patronymicName(),
                    request.password()
            );

            default -> {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }

        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        if (user.getRole() == UserRole.PATIENT) {
            Patient patient = accountService.getPatient(user.getId());

            return ResponseEntity.ok(new PatientResponse(
                    new UserResponse(user.getId(), user.getLastName(),
                            user.getFirstName(), user.getPatronymicName()),
                    patient.isGender()
            ));
        }

        if (user.getRole() == UserRole.DOCTOR) {
            Doctor doctor = accountService.getDoctor(user.getId());
            Specialization specialization = doctor.getSpecialization();

            return ResponseEntity.ok(new DoctorResponse(
                    new UserResponse(user.getId(), user.getLastName(),
                            user.getFirstName(), user.getPatronymicName()),
                    specialization == null ? null :
                            new SpecializationResponse(
                                    specialization.getId(),
                                    specialization.getName())
            ));
        }

        if (user.getRole() == UserRole.ADMIN) {
            return ResponseEntity.ok(new AdminResponse(
                    new UserResponse(user.getId(), user.getLastName(),
                            user.getFirstName(), user.getPatronymicName())
            ));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
