package io.github.lb357.hospital_tp_miit.service;

import io.github.lb357.hospital_tp_miit.model.appointment.Appointment;
import io.github.lb357.hospital_tp_miit.model.checkup.Checkup;
import io.github.lb357.hospital_tp_miit.model.specialization.Specialization;
import io.github.lb357.hospital_tp_miit.model.user.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CheckupService {
    private final EntityManager entityManager;
    private final AppointmentService appointmentService;

    public Checkup createCheckup(Patient patient, List<Specialization> specializations) {
        Checkup checkup = new Checkup(patient, specializations);
        entityManager.persist(checkup);
        return checkup;
    }

    public void deleteCheckup(Checkup checkup) {
        if (checkup != null && entityManager.contains(checkup)) entityManager.remove(checkup);
    }

    public List<Checkup> getAllCheckups() {
        return entityManager.createQuery("SELECT c FROM Checkup c", Checkup.class).getResultList();
    }

    public List<Checkup> getPatientCheckups(Patient patient) {
        return entityManager.createQuery(
                "SELECT c FROM Checkup c WHERE c.patient = :patient", Checkup.class
        ).setParameter("patient", patient).getResultList();
    }

    public Checkup getCheckup(Integer id) {
        return entityManager.find(Checkup.class, id);
    }

    public Boolean isCheckupCompleted(Checkup checkup) {
        List<Appointment> appointments = entityManager.createQuery(
                "SELECT a FROM Appointment a WHERE a.checkup = :checkup", Appointment.class
        ).setParameter("checkup", checkup).getResultList();
        return !appointments.isEmpty() && appointments.stream().allMatch(appointmentService::isAppointmentCompleted);
    }
}
