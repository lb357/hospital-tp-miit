package io.github.lb357.hospital_tp_miit.model.checkup;
import io.github.lb357.hospital_tp_miit.model.specialization.Specialization;
import io.github.lb357.hospital_tp_miit.model.user.Patient;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "checkups")
public class Checkup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false, referencedColumnName = "id", foreignKey = @ForeignKey(name="fk_checkup_patient"))
    private Patient patient;

    @ManyToMany
    @JoinTable(
            name = "checkup_specializations",
            joinColumns = @JoinColumn(name = "checkup_id"),
            inverseJoinColumns = @JoinColumn(name = "specialization_id"),
            foreignKey = @ForeignKey(name="fk_checkup_specializations")
    )
    private List<Specialization> specializations;

    public Checkup(Integer id, Patient patient, List<Specialization> specializations) {
        this.id = id;
        this.patient = patient;
        this.specializations = specializations;
    }

    protected Checkup() {}

    public Integer getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public List<Specialization> getSpecializations() {
        return specializations;
    }


}
