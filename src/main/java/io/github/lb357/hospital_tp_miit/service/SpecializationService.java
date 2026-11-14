package io.github.lb357.hospital_tp_miit.service;

import io.github.lb357.hospital_tp_miit.model.specialization.Specialization;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SpecializationService {
    private final EntityManager entityManager;

    public Specialization createSpecialization(String name) {
        //todo
        return null;
    }

    public void deleteSpecialization(Specialization specialization) {
        //todo
    }

    public Specialization getSpecialization(Integer id) {
        //todo
        return null;
    }

    public List<Specialization> getSpecializations() {
        //todo
        return null;
    }
}
