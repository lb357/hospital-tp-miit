package io.github.lb357.hospital_tp_miit.model.specialization;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "specializations")
public class Specialization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @Column(nullable = false, unique = true)
    private String name;

    public Specialization(String name) {
        this.name = name;
    }

    protected Specialization() {}

}