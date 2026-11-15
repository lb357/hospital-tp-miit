package io.github.lb357.hospital_tp_miit.model.appointment;

import io.github.lb357.hospital_tp_miit.model.checkup.Checkup;
import io.github.lb357.hospital_tp_miit.model.user.Doctor;
import io.github.lb357.hospital_tp_miit.model.user.Patient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", foreignKey = @ForeignKey(name="fk_appointment_patient"))
    private Patient patient;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", foreignKey = @ForeignKey(name="fk_appointment_doctor"))
    private Doctor doctor;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkup_id", referencedColumnName = "id", foreignKey = @ForeignKey(name="fk_appointment_checkup"))
    private Checkup checkup;

    @Setter
    private String room;

    @Setter
    private LocalDateTime appointedTime;

    public Appointment(Doctor doctor, String room, LocalDateTime appointedTime) {
        this.doctor = doctor;
        this.room = room;
        this.appointedTime = appointedTime;
    }

    protected Appointment() {}


}