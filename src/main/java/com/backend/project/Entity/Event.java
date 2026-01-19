package com.backend.project.Entity;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name="events")

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 100)
    private String eventId;

    @Column(nullable = false)
    private Instant eventTime;

    @Column(nullable = false)
    private Instant receivedTime;
    @PrePersist
    public void onCreate() {
        if (this.receivedTime == null) {
            this.receivedTime = Instant.now();
        }
    }
    @Column(nullable = false)
    private String factoryId;

    @Column(nullable = false, length = 50)
    private String machineId;

    @Column(length = 50)
    private String lineId;

    @Column(nullable = false)
    private Long durationMs;

    @Column(nullable = false)
    private Integer defectCount;

}
