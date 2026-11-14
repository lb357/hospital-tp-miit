package io.github.lb357.hospital_tp_miit.model.user;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
public class Admin extends User {
    public Admin(String lastName, String firstName, String patronymicName, String passwordHash) {
        super(lastName, firstName, patronymicName, passwordHash, UserRole.ADMIN);
    }
    protected Admin() {}
}