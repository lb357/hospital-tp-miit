package io.github.lb357.hospital_tp_miit.model.user;

import jakarta.persistence.*;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "user_id")
public class Admin extends User {
    public Admin(Integer id, String lastName, String firstName, String patronymicName, String passwordHash, UserRole role) {
        super(id, lastName, firstName, patronymicName, passwordHash, role);
    }
    protected Admin() {}
}