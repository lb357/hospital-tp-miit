package io.github.lb357.hospital_tp_miit.model.specialization;

import jakarta.persistence.*;

@Entity
@Table(name = "specializations")
public class Specialization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    public Specialization(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    protected Specialization() {}

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}