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

    public Entry createEntry(Appointment appointment, String content) {
        //todo
        return null;
    }

    public void updateEntry(String content, Entry entry) {
        //todo
    }

    public List<Entry> getPatientEntries(Patient patient) {
        //todo
        return null;
    }
}
