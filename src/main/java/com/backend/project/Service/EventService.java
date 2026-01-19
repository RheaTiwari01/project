package com.backend.project.Service;

import com.backend.project.DTO.IngestDto;
import com.backend.project.DTO.Rejection;
import com.backend.project.Entity.Event;
import com.backend.project.repository.EventRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepo eventRepo;

    public EventService(EventRepo eventRepo) {
        this.eventRepo = eventRepo;
    }

    @Transactional
    public synchronized IngestDto ingest(List<Event> events) {

        int accepted = 0;
        int updated = 0;
        int deduped = 0;
        int rejected = 0;

        List<Rejection> rejections = new ArrayList<>();

        for (Event e : events) {


            if (e.getDurationMs() < 0 || e.getDurationMs() > 21600000) {
                rejected++;
                rejections.add(new Rejection(e.getEventId(), "INVALID_DURATION"));
                continue;
            }

            // 🔴 FUTURE EVENT
            if (e.getEventTime().isAfter(Instant.now())) {
                rejected++;
                rejections.add(new Rejection(e.getEventId(), "FUTURE_EVENT"));
                continue;
            }

            // ✅ ENSURE receivedTime EXISTS
            if (e.getReceivedTime() == null) {
                e.setReceivedTime(Instant.now());
            }

            Optional<Event> existingOpt =
                    eventRepo.findByEventId(e.getEventId());

            if (existingOpt.isPresent()) {

                Event old = existingOpt.get();

                // 1️⃣ Older → ignore
                if (e.getReceivedTime().isBefore(old.getReceivedTime())) {
                    continue;
                }

                // 2️⃣ Exact duplicate → dedup
                if (old.getDurationMs().equals(e.getDurationMs())
                        && old.getDefectCount().equals(e.getDefectCount())
                        && old.getEventTime().equals(e.getEventTime())) {

                    deduped++;
                    continue;
                }

                // 3️⃣ Update
                old.setEventTime(e.getEventTime());
                old.setDurationMs(e.getDurationMs());
                old.setDefectCount(e.getDefectCount());
                old.setReceivedTime(e.getReceivedTime());

                eventRepo.save(old);
                updated++;

            } else {
                // New event
                eventRepo.save(e);
                accepted++;
            }
        }

        return new IngestDto(
                accepted,
                deduped,
                updated,
                rejected,
                rejections
        );
    }
}
