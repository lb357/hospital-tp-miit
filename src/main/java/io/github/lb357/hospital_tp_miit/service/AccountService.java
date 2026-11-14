package io.github.lb357.hospital_tp_miit.service;


import io.github.lb357.hospital_tp_miit.model.session.Session;
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
public class AccountService {
    private final EntityManager entityManager;

    public String login(Integer id, String password) {
        //todo
        return "";
    }

    public void logout(String token) {
        //todo
    }

    public void deleteUser(User user) {

    }

    public void updateUser(User user, String lastName, String firstName, String patronymicName) {
        //todo
    }

    public User getUser(Session session) {
        //todo
        return null;
    }

    public User getUser(Integer id) {
        //todo
        return null;
    }

    public Session getSession(String token) {
        //todo
        return null;
    }

    public Patient signupPatient(String lastName, String firstName, String patronymicName, String password, Boolean gender) {
        //todo
        return null;
    }

    public Doctor signupDoctor(String lastName, String firstName, String patronymicName, String password, Specialization specialization) {
        //todo
        return null;
    }

    public Admin signupAdmin(String lastName, String firstName, String patronymicName, String password) {
        //todo
        return null;
    }

    public List<User> getAllUsers() {
        //todo
        return null;
    }

    public Patient getPatient(Integer id) {
        //todo
        return null;
    }

    public Doctor getDoctor(Integer id) {
        //todo
        return null;
    }

    public Admin getAdmin(Integer id) {
        //todo
        return null;
    }

    public List<Doctor> getDoctorsBySpecialization(Specialization specialization) {
        //todo
        return null;
    }
}
