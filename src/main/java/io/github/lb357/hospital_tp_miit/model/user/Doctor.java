package io.github.lb357.hospital_tp_miit.model.user;

import io.github.lb357.hospital_tp_miit.model.specialization.Specialization;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "doctors")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
public class Doctor extends User {
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id", referencedColumnName = "id", foreignKey = @ForeignKey(name="fk_doctor_specialization"))
    private Specialization specialization;

    public Doctor(String lastName, String firstName, String patronymicName, String passwordHash, Specialization specialization) {
        super(lastName, firstName, patronymicName, passwordHash, UserRole.DOCTOR);
        this.specialization = specialization;
    }

    protected Doctor() {}
}