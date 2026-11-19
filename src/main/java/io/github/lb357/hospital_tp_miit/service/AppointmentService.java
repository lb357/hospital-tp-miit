package io.github.lb357.hospital_tp_miit.service;

import io.github.lb357.hospital_tp_miit.model.appointment.Appointment;
import io.github.lb357.hospital_tp_miit.model.checkup.Checkup;
import io.github.lb357.hospital_tp_miit.model.entry.Entry;
import io.github.lb357.hospital_tp_miit.model.user.Doctor;
import io.github.lb357.hospital_tp_miit.model.user.Patient;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static java.lang.Math.abs;

@Service
@Transactional
@RequiredArgsConstructor
public class AppointmentService {
    private final EntityManager entityManager;

    public Appointment createAppointment(Doctor doctor, String room, LocalDateTime appointedTime) {
        Appointment appointment = new Appointment(doctor, room, appointedTime);
        entityManager.persist(appointment);
        return appointment;
    }

    public void deleteAppointment(Appointment appointment) {
        if (appointment != null && entityManager.contains(appointment)) entityManager.remove(appointment);
    }

    public List<Appointment> getAllAppointments() {
        return entityManager.createQuery("SELECT a FROM Appointment a", Appointment.class).getResultList();
    }

    public List<Appointment> getDoctorAppointments(Doctor doctor) {
        return entityManager.createQuery("SELECT a FROM Appointment a WHERE a.doctor = :doctor",
                        Appointment.class
                ).setParameter("doctor", doctor).getResultList();
    }

    public List<Appointment> getPatientAppointments(Patient patient) {
        return entityManager.createQuery("SELECT a FROM Appointment a WHERE a.patient = :patient",
                Appointment.class
        ).setParameter("patient", patient
        ).getResultStream().filter(appointment -> !isAppointmentCompleted(appointment)).toList();
    }

    public List<Appointment> getCompletedPatientAppointments(Patient patient) {
        return entityManager.createQuery("SELECT a FROM Appointment a WHERE a.patient = :patient",
                Appointment.class
        ).setParameter("patient", patient
        ).getResultStream().toList();
    }

    public Boolean addPatientToAppointment(Appointment appointment, Patient patient) {
        if (appointment.getPatient() != null) return false;
        if (getPatientAppointments(patient).stream().anyMatch(otherPatientAppointment ->
            abs(Duration.between(appointment.getAppointedTime(), otherPatientAppointment.getAppointedTime()).toMinutes()) < 15
                    ||
            appointment.getDoctor().getSpecialization().equals(otherPatientAppointment.getDoctor().getSpecialization())
        )) return false;
        appointment.setPatient(patient);
        return true;
    }

    public Boolean addPatientToAppointment(Appointment appointment, Checkup patientCheckup) {
        if (!addPatientToAppointment(appointment, patientCheckup.getPatient())) return false;
        appointment.setCheckup(patientCheckup);
        return true;
    }

    public void removePatientFromAppointment(Appointment appointment) {
        appointment.setPatient(null);
        appointment.setCheckup(null);
    }

    public Appointment getAppointment(Integer id) {
        return entityManager.find(Appointment.class, id);
    }

    public Boolean isAppointmentCompleted(Appointment appointment) {
        return entityManager.createQuery(
                "SELECT e FROM Entry e WHERE e.appointment = :appointment", Entry.class
        ).setParameter("appointment", appointment).getSingleResultOrNull() != null
                &&
        appointment.getAppointedTime().isBefore(LocalDateTime.now());
    }
}
