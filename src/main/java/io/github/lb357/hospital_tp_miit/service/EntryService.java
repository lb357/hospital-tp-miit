package io.github.lb357.hospital_tp_miit.service;

import io.github.lb357.hospital_tp_miit.model.appointment.Appointment;
import io.github.lb357.hospital_tp_miit.model.entry.Entry;
import io.github.lb357.hospital_tp_miit.model.user.Patient;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EntryService {
    private final EntityManager entityManager;
    private final AppointmentService appointmentService;

    public Entry createEntry(Appointment appointment, String content) {
        Entry entry = new Entry(appointment, content);
        entityManager.persist(entry);
        return entry;
    }

    public void updateEntry(String content, Entry entry) {
        if (content != null && !content.isBlank()) entry.setContent(content);
    }

    public Entry getAppointmentEntry(Appointment appointment) {
        return entityManager.createQuery(
                "SELECT e FROM Entry e WHERE e.appointment = :appointment", Entry.class
        ).setParameter("appointment", appointment).getSingleResultOrNull();
    }

    public List<Entry> getPatientEntries(Patient patient) {
        return appointmentService.getPatientAppointments(patient).stream().map(this::getAppointmentEntry).toList();
    }
}
