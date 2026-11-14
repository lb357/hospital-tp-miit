package io.github.lb357.hospital_tp_miit.model.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    private String lastName;

    @Setter
    private String firstName;

    @Setter
    private String patronymicName;

    @Setter
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    protected User(String lastName, String firstName, String patronymicName, String passwordHash, UserRole role) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.patronymicName = patronymicName;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    protected User() {}

}
