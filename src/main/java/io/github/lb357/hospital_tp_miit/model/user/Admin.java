package io.github.lb357.hospital_tp_miit.model.user;

import jakarta.persistence.*;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "user_id")
public class Admin extends User {
    protected Admin() {}
}