package io.github.lb357.hospital_tp_miit.service;

import io.github.lb357.hospital_tp_miit.model.appointment.Appointment;
import io.github.lb357.hospital_tp_miit.model.checkup.Checkup;
import io.github.lb357.hospital_tp_miit.model.specialization.Specialization;
import io.github.lb357.hospital_tp_miit.model.user.Doctor;
import io.github.lb357.hospital_tp_miit.model.user.Patient;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AppointmentService {
    private final EntityManager entityManager;

    public Appointment createAppointment(Doctor doctor, String room, LocalDateTime appointedTime) {
        //todo
        return null;
    }

    public void createAppointment(Appointment appointment) {
        //todo
    }

    public List<Appointment> getAllAppointments() {
        //todo
        return null;
    }

    public List<Appointment> getDoctorAppointments(Doctor doctor) {
        //todo
        return null;
    }

    public List<Appointment> getPatientAppointments(Patient patient) {
        //todo
        return null;
    }

    public void addPatientToAppointment(Appointment appointment, Patient patient) {
        //todo
    }

    public void addPatientToAppointment(Appointment appointment, Checkup patientCheckup) {
        //todo
    }

    public void removePatientFromAppointment(Appointment appointment) {
        //todo
    }

    public Appointment getAppointment(Integer id) {
        //todo
        return null;
    }

    public Boolean isAppointmentCompleted(Appointment appointment) {
        //todo
        return false;
    }
}
