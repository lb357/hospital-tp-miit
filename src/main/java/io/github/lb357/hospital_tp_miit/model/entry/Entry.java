package io.github.lb357.hospital_tp_miit.model.entry;

import io.github.lb357.hospital_tp_miit.model.appointment.Appointment;
import jakarta.persistence.*;


@Entity
@Table(name = "entries")
public class Entry {
    @Id
    private Integer id;
    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", referencedColumnName = "id", foreignKey = @ForeignKey(name="fk_entry_appointment"))
    private Appointment appointment;

    private String content;

    public Entry(Appointment appointment, String content) {
        this.appointment = appointment;
        this.content = content;
    }

    protected Entry() {}

    public Appointment getAppointment() {
        return appointment;
    }

    public String getContent() {
        return content;
    }
}
