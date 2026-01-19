package com.backend.project.repository;

import com.backend.project.Entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.*;

import java.time.Instant;
import java.util.*;

@Repository
public interface EventRepo extends JpaRepository<Event, Long> {

    Optional<Event> findByEventId(String eventId);


    List<Event> findByMachineIdAndEventTimeBetween(
            String machineId,
            Instant start,
            Instant end
    );

    List<Event> findByFactoryIdAndEventTimeBetween(
            String factoryId,
            Instant start,
            Instant end
    );

}
