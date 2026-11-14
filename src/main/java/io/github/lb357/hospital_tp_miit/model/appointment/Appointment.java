package io.github.lb357.hospital_tp_miit.model.appointment;

import io.github.lb357.hospital_tp_miit.model.checkup.Checkup;
import io.github.lb357.hospital_tp_miit.model.user.Doctor;
import io.github.lb357.hospital_tp_miit.model.user.Patient;
import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", foreignKey = @ForeignKey(name="fk_appointment_patient"))
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", foreignKey = @ForeignKey(name="fk_appointment_doctor"))
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkup_id", referencedColumnName = "id", foreignKey = @ForeignKey(name="fk_appointment_checkup"))
    private Checkup checkup;

    private String room;

    private LocalDateTime appointedTime;

    public Appointment(Integer id, Patient patient, Doctor doctor, Checkup checkup, String room, LocalDateTime appointedTime) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.checkup = checkup;
        this.room = room;
        this.appointedTime = appointedTime;
    }

    protected Appointment() {}

    public Integer getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Checkup getCheckup() {
        return checkup;
    }

    public String getRoom() {
        return room;
    }

    public LocalDateTime getAppointedTime() {
        return appointedTime;
    }
}