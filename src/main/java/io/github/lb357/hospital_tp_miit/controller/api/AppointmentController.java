package io.github.lb357.hospital_tp_miit.controller.api;

import io.github.lb357.hospital_tp_miit.component.CookieTokenResolver;
import io.github.lb357.hospital_tp_miit.dto.*;
import io.github.lb357.hospital_tp_miit.model.appointment.Appointment;
import io.github.lb357.hospital_tp_miit.model.checkup.Checkup;
import io.github.lb357.hospital_tp_miit.model.entry.Entry;
import io.github.lb357.hospital_tp_miit.model.session.Session;
import io.github.lb357.hospital_tp_miit.model.user.Doctor;
import io.github.lb357.hospital_tp_miit.model.user.Patient;
import io.github.lb357.hospital_tp_miit.model.user.User;
import io.github.lb357.hospital_tp_miit.model.user.UserRole;
import io.github.lb357.hospital_tp_miit.service.AccountService;
import io.github.lb357.hospital_tp_miit.service.AppointmentService;
import io.github.lb357.hospital_tp_miit.service.CheckupService;
import io.github.lb357.hospital_tp_miit.service.EntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
public class AppointmentController {
    private final CookieTokenResolver cookieTokenResolver;
    private final AccountService accountService;
    private final AppointmentService appointmentService;
    private final CheckupService checkupService;
    private final EntryService entryService;

    @PostMapping("/create")
    public ResponseEntity<AppointmentResponse> create(@RequestBody AppointmentCreateRequest request) {
        Session session = cookieTokenResolver.getSession();
        if (session == null || !accountService.getUser(session).getRole().equals(UserRole.ADMIN))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Doctor doctor = accountService.getDoctor(request.doctorId());
        String room = request.room();
        LocalDateTime appointedTime = request.appointedTime();

        Appointment appointment = appointmentService.createAppointment(doctor, room, appointedTime);
        return ResponseEntity.ok(new AppointmentResponse(
                appointment.getId(),
                null,
                new DoctorResponse(
                        new UserResponse(
                                doctor.getId(),
                                doctor.getLastName(),
                                doctor.getFirstName(),
                                doctor.getPatronymicName()
                        ),
                        new SpecializationResponse(
                                doctor.getSpecialization().getId(),
                                doctor.getSpecialization().getName()
                        )
                ),
                null,
                room,
                appointedTime
        ));
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> delete(@RequestBody AppointmentDeleteRequest request) {
        Session session = cookieTokenResolver.getSession();
        if (session == null || !accountService.getUser(session).getRole().equals(UserRole.ADMIN))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        Appointment appointment = appointmentService.getAppointment(request.appointmentId());
        if (appointment!=null) {
            appointmentService.deleteAppointment(appointment);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/get")
    public ResponseEntity<List<AppointmentResponse>> get() {
        Session session = cookieTokenResolver.getSession();
        User user = accountService.getUser(session);
        if (session == null || user == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        List<Appointment> appointments;
        switch (user.getRole()) {
            case ADMIN -> appointments = appointmentService.getAllAppointments();
            case DOCTOR -> appointments = appointmentService.getDoctorAppointments(accountService.getDoctor(user.getId()));
            case PATIENT -> appointments = appointmentService.getPatientAppointments(accountService.getPatient(user.getId()));
            default -> appointments = List.of();
        }
        return ResponseEntity.ok(appointments.stream().map(appointment ->
                new AppointmentResponse(
                        appointment.getId(),
                        appointment.getPatient()!=null?new PatientResponse(
                                new UserResponse(
                                        appointment.getPatient().getId(),
                                        appointment.getPatient().getLastName(),
                                        appointment.getPatient().getFirstName(),
                                        appointment.getPatient().getPatronymicName()
                                ),
                                appointment.getPatient().isGender()
                        ):null,
                        new DoctorResponse(
                                new UserResponse(
                                        appointment.getDoctor().getId(),
                                        appointment.getDoctor().getLastName(),
                                        appointment.getDoctor().getFirstName(),
                                        appointment.getDoctor().getPatronymicName()
                                ),
                                new SpecializationResponse(
                                        appointment.getDoctor().getSpecialization().getId(),
                                        appointment.getDoctor().getSpecialization().getName()
                                )
                        ),
                        appointment.getCheckup()!=null?appointment.getCheckup().getId():null,
                        appointment.getRoom(),
                        appointment.getAppointedTime()
                )
        ).toList());
    }

    @PostMapping("/add")
    public ResponseEntity<SuccessResponse> add(@RequestBody AppointmentAddRequest request) {
        Session session = cookieTokenResolver.getSession();
        User user = accountService.getUser(session);
        if (session == null || user == null || !user.getRole().equals(UserRole.PATIENT))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        Appointment appointment = appointmentService.getAppointment(request.appointmentId());
        if (appointment == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        Boolean success = false;
        if (request.checkupId()==null) {
            success = appointmentService.addPatientToAppointment(appointment, accountService.getPatient(user.getId()));
        } else {
            Checkup checkup = checkupService.getCheckup(request.checkupId());
            if (checkup==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            if (!checkup.getPatient().getId().equals(user.getId())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            success = appointmentService.addPatientToAppointment(appointment, checkup);
        }
        return ResponseEntity.ok(new SuccessResponse(success));
    }

    @PostMapping("/entry/create")
    public ResponseEntity<EntryResponse> entryCreate(@RequestBody EntryCreateRequest request) {
        Session session = cookieTokenResolver.getSession();
        User user = accountService.getUser(session);
        if (session == null || user == null || !user.getRole().equals(UserRole.DOCTOR))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        Appointment appointment = appointmentService.getAppointment(request.appointmentId());
        if (appointment == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        if (!appointment.getDoctor().getId().equals(user.getId())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (request.content() == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        Entry entry = entryService.createEntry(appointment, request.content());
        return ResponseEntity.ok(new EntryResponse(
                appointment.getId(),
                appointment.getAppointedTime(),
                new DoctorResponse(
                        new UserResponse(
                                appointment.getDoctor().getId(),
                                appointment.getDoctor().getLastName(),
                                appointment.getDoctor().getFirstName(),
                                appointment.getDoctor().getPatronymicName()
                        ),
                        new SpecializationResponse(
                                appointment.getDoctor().getSpecialization().getId(),
                                appointment.getDoctor().getSpecialization().getName()
                        )
                ),
                entry.getContent()
        ));
    }

    @PostMapping("/entry/update")
    public ResponseEntity<EntryResponse> entryUpdate(@RequestBody EntryUpdateRequest request) {
        Session session = cookieTokenResolver.getSession();
        User user = accountService.getUser(session);
        if (session == null || user == null || !user.getRole().equals(UserRole.DOCTOR))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        Appointment appointment = appointmentService.getAppointment(request.appointmentId());
        if (appointment == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        if (!appointment.getDoctor().getId().equals(user.getId())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        Entry entry = entryService.getAppointmentEntry(appointment);
        if (entry == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        if (request.content() == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        entryService.updateEntry(request.content(), entry);
        return ResponseEntity.ok(new EntryResponse(
                appointment.getId(),
                appointment.getAppointedTime(),
                new DoctorResponse(
                        new UserResponse(
                                appointment.getDoctor().getId(),
                                appointment.getDoctor().getLastName(),
                                appointment.getDoctor().getFirstName(),
                                appointment.getDoctor().getPatronymicName()
                        ),
                        new SpecializationResponse(
                                appointment.getDoctor().getSpecialization().getId(),
                                appointment.getDoctor().getSpecialization().getName()
                        )
                ),
                entry.getContent()
        ));
    }

    @PostMapping("/entry/get")
    public ResponseEntity<EntryResponse> entryGet(@RequestBody EntryGetRequest request){
        Session session = cookieTokenResolver.getSession();
        User user = accountService.getUser(session);
        if (session == null || user == null || !(user.getRole().equals(UserRole.DOCTOR)||user.getRole().equals(UserRole.ADMIN)))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        Appointment appointment = appointmentService.getAppointment(request.appointmentId());
        if (appointment == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        Entry entry = entryService.getAppointmentEntry(appointment);
        if (entry == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(new EntryResponse(
                appointment.getId(),
                appointment.getAppointedTime(),
                new DoctorResponse(
                        new UserResponse(
                                appointment.getDoctor().getId(),
                                appointment.getDoctor().getLastName(),
                                appointment.getDoctor().getFirstName(),
                                appointment.getDoctor().getPatronymicName()
                        ),
                        new SpecializationResponse(
                                appointment.getDoctor().getSpecialization().getId(),
                                appointment.getDoctor().getSpecialization().getName()
                        )
                ),
                entry.getContent()
        ));
    }

    @PostMapping("/entry/record")
    public ResponseEntity<List<EntryResponse>> record(@RequestBody RecordRequest request){
        Session session = cookieTokenResolver.getSession();
        User user = accountService.getUser(session);
        if (session == null || user == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        Patient patient = accountService.getPatient(request.patientId());
        if (patient == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        if(user.getRole().equals(UserRole.PATIENT)&&!user.getId().equals(request.patientId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        List<Entry> entries = entryService.getPatientEntries(patient);
        if (entries == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(entries.stream().map(entry->{
            Appointment appointment = entry.getAppointment();
            return new EntryResponse(
                    appointment.getId(),
                    appointment.getAppointedTime(),
                    new DoctorResponse(
                            new UserResponse(
                                    appointment.getDoctor().getId(),
                                    appointment.getDoctor().getLastName(),
                                    appointment.getDoctor().getFirstName(),
                                    appointment.getDoctor().getPatronymicName()
                            ),
                            new SpecializationResponse(
                                    appointment.getDoctor().getSpecialization().getId(),
                                    appointment.getDoctor().getSpecialization().getName()
                            )
                    ),
                    entry.getContent()
            );
        }).toList());
    }
}
