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
        Specialization specialization = new Specialization(name);
        entityManager.persist(specialization);
        return specialization;
    }

    public void deleteSpecialization(Specialization specialization) {
        if (specialization != null && entityManager.contains(specialization)) entityManager.remove(specialization);
    }

    public Specialization getSpecialization(Integer id) {
        if (id == null) return null;
        return entityManager.find(Specialization.class, id);
    }

    public List<Specialization> getSpecializations() {
        return entityManager.createQuery("SELECT s FROM Specialization s", Specialization.class).getResultList();
    }
}
