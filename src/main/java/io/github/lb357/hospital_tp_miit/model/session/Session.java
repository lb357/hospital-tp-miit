package io.github.lb357.hospital_tp_miit.model.session;

import io.github.lb357.hospital_tp_miit.model.user.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "sessions")
public class Session {
    @Id
    @Column(nullable = false, unique = true)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id", foreignKey = @ForeignKey(name="fk_session_user"))
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryTime;


    public Session(String tokenHash, User user, LocalDateTime expiryTime) {
        this.tokenHash = tokenHash;
        this.user = user;
        this.expiryTime = expiryTime;
    }

    protected Session() {}


}
