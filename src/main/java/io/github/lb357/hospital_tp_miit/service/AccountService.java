package io.github.lb357.hospital_tp_miit.service;


import io.github.lb357.hospital_tp_miit.component.Hasher;
import io.github.lb357.hospital_tp_miit.model.session.Session;
import io.github.lb357.hospital_tp_miit.model.specialization.Specialization;
import io.github.lb357.hospital_tp_miit.model.user.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {
    private final EntityManager entityManager;
    private final Hasher hasher;

    public String login(Integer id, String password) {
        if (id==null||password==null) return null;
        User user = entityManager.find(User.class, id);
        if (user == null || !user.getPasswordHash().equals(hasher.hash(password))) return null;
        String token = UUID.randomUUID().toString();
        entityManager.persist(new Session(hasher.hash(token), user, LocalDateTime.now().plusDays(7)));
        return token;
    }

    public void logout(String token) {
        Session session = entityManager.find(Session.class, hasher.hash(token));
        if (session != null) entityManager.remove(session);
    }

    public void deleteUser(User user) {
        if (user != null && entityManager.contains(user)) entityManager.remove(user);
    }

    public void updateUser(User user, String lastName, String firstName, String patronymicName) {
        if (lastName!=null && !lastName.isBlank()) user.setLastName(lastName);
        if (firstName!=null && !firstName.isBlank()) user.setFirstName(firstName);
        if (patronymicName!=null && !patronymicName.isBlank()) user.setPatronymicName(patronymicName);
    }

    public User getUser(Session session) {
        if(session==null) return null;
        return session.getUser();
    }

    public User getUser(Integer id) {
        if (id==null) return null;
        return entityManager.find(User.class, id);
    }

    public Session getSession(String token) {
        if (token==null) return null;
        return entityManager.find(Session.class, hasher.hash(token));
    }

    public Patient signupPatient(String lastName, String firstName, String patronymicName, String password, Boolean gender) {
        if (lastName==null||firstName==null||patronymicName==null||password==null||gender==null) return null;
        Patient patient = new Patient(lastName, firstName, patronymicName, hasher.hash(password), gender);
        entityManager.persist(patient);
        return patient;
    }

    public Doctor signupDoctor(String lastName, String firstName, String patronymicName, String password, Specialization specialization) {
        if (lastName==null||firstName==null||patronymicName==null||password==null||specialization==null) return null;
        Doctor doctor = new Doctor(lastName, firstName, patronymicName, hasher.hash(password), specialization);
        entityManager.persist(doctor);
        return doctor;
    }

    public Admin signupAdmin(String lastName, String firstName, String patronymicName, String password) {
        if (lastName==null||firstName==null||patronymicName==null||password==null) return null;
        Admin admin = new Admin(lastName, firstName, patronymicName, hasher.hash(password));
        entityManager.persist(admin);
        return admin;
    }

    public List<User> getAllUsers() {
        return entityManager.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    public Patient getPatient(Integer id) {
        if(id==null) return null;
        return entityManager.find(Patient.class, id);
    }

    public Doctor getDoctor(Integer id) {
        if(id==null) return null;
        return entityManager.find(Doctor.class, id);
    }

    public Admin getAdmin(Integer id) {
        if(id==null) return null;
        return entityManager.find(Admin.class, id);
    }

    public Admin getAdmin(Session session) {
        if(session==null) return null;
        return getAdmin(session.getUser().getId());
    }

    public List<Doctor> getDoctorsBySpecialization(Specialization specialization) {
        return entityManager.createQuery(
                "SELECT d FROM Doctor d WHERE d.specialization = :specialization",
                        Doctor.class
                ).setParameter("specialization", specialization)
                .getResultList();
    }
}
