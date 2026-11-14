package io.github.lb357.hospital_tp_miit.model.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "patients")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
public class Patient extends User {
    @Setter
    private boolean gender;

    public Patient(String lastName, String firstName, String patronymicName, String passwordHash, boolean gender) {
        super(lastName, firstName, patronymicName, passwordHash, UserRole.PATIENT);
        this.gender = gender;
    }

    protected Patient() {}
}