package io.github.lb357.hospital_tp_miit.model.user;

import jakarta.persistence.*;

@Entity
@Table(name = "patients")
@PrimaryKeyJoinColumn(name = "user_id")
public class Patient extends User {
    private boolean gender;
    protected Patient() {}

    public boolean getGender() {
        return gender;
    }
}