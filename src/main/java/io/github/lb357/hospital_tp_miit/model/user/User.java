package io.github.lb357.hospital_tp_miit.model.user;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String lastName;

    private String firstName;

    private String patronymicName;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    protected User(Integer id, String lastName, String firstName, String patronymicName, String passwordHash, UserRole role) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.patronymicName = patronymicName;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    protected User() {}

    public Integer getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPatronymicName() {
        return patronymicName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }
}
