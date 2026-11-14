package io.github.lb357.hospital_tp_miit.model.entry;

import io.github.lb357.hospital_tp_miit.model.appointment.Appointment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "entries")
@Getter
public class Entry {
    @Id
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", referencedColumnName = "id", foreignKey = @ForeignKey(name="fk_entry_appointment"))
    private Appointment appointment;

    @Setter
    private String content;

    public Entry(Appointment appointment, String content) {
        this.appointment = appointment;
        this.content = content;
    }

    protected Entry() {}

}
