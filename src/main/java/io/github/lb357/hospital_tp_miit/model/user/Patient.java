package io.github.lb357.hospital_tp_miit.model.user;

import jakarta.persistence.*;

@Entity
@Table(name = "patients")
@PrimaryKeyJoinColumn(name = "user_id")
public class Patient extends User {
    private boolean gender;

    public Patient(Integer id, String lastName, String firstName, String patronymicName, String passwordHash, UserRole role, boolean gender) {
        super(id, lastName, firstName, patronymicName, passwordHash, role);
        this.gender = gender;
    }

    protected Patient() {}

    public boolean getGender() {
        return gender;
    }
}