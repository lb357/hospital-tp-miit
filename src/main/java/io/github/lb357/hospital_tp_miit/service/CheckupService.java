package io.github.lb357.hospital_tp_miit.service;

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

    public Checkup createCheckup(Patient patient, List<Specialization> specializations) {
        //todo
        return null;
    }

    public void deleteCheckup(Checkup checkup) {
        //todo
    }

    public List<Checkup> getAllCheckups() {
        //todo
        return null;
    }

    public List<Checkup> getPatientCheckups(Patient patient) {
        //todo
        return null;
    }

    public Checkup getCheckup(Integer id) {
        //todo
        return null;
    }

    public Boolean isCheckupCompleted(Checkup checkup) {
        //todo
        return null;
    }
}
