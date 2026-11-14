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

    protected Specialization() {}
}