package io.github.lb357.hospital_tp_miit.model.user;

import io.github.lb357.hospital_tp_miit.model.specialization.Specialization;
import jakarta.persistence.*;

@Entity
@Table(name = "doctors")
@PrimaryKeyJoinColumn(name = "user_id")
public class Doctor extends User {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id")
    private Specialization specialization;

    protected Doctor() {}

    public void setSpecialization(Specialization specialization) {
        this.specialization = specialization;
    }
}