package com.backend.project.Service;

import com.backend.project.DTO.StatDto;
import com.backend.project.DTO.TopDefectLineDto;
import com.backend.project.Entity.Event;
import com.backend.project.repository.EventRepo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.*;

@Service
public class StatService {

    private final EventRepo eventRepo;

    public StatService(EventRepo eventRepo) {
        this.eventRepo = eventRepo;
    }

    @Transactional(readOnly = true)
    public StatDto getStats(String machineId, Instant start, Instant end) {

        List<Event> events =
                eventRepo.findByMachineIdAndEventTimeBetween(machineId, start, end);

        long count = events.size();

        long defects = events.stream()
                .filter(e -> e.getDefectCount() != -1)
                .mapToLong(Event::getDefectCount)
                .sum();
        double hours = Duration.between(start, end).toSeconds() / 3600.0;
        double rate = (hours == 0) ? 0 : defects / hours;
        return new StatDto(
                machineId,
                start.toString(),
                end.toString(),
                count,
                defects,
                rate,
                rate < 2 ? "Healthy" : "Warning"
        );
    }

    @Transactional(readOnly = true)
    public List<TopDefectLineDto> getTopDefectLines(
            String factoryId,
            Instant from,
            Instant to,
            int limit
    ) {

        List<Event> events =
                eventRepo.findByFactoryIdAndEventTimeBetween(factoryId, from, to);

        Map<String, Long> defectSum = new HashMap<>();
        Map<String, Long> eventCount = new HashMap<>();

        for (Event e : events) {

            if (e.getLineId() == null) continue;

            eventCount.put(
                    e.getLineId(),
                    eventCount.getOrDefault(e.getLineId(), 0L) + 1
            );

            if (e.getDefectCount() != -1) {
                defectSum.put(
                        e.getLineId(),
                        defectSum.getOrDefault(e.getLineId(), 0L) + e.getDefectCount()
                );
            }
        }

        List<TopDefectLineDto> result = new ArrayList<>();

        for (String line : defectSum.keySet()) {
            long defects = defectSum.get(line);
            long count = eventCount.get(line);

            double percent = count == 0 ? 0 : (defects * 100.0) / count;

            result.add(new TopDefectLineDto(
                    line,
                    defects,
                    count,
                    Math.round(percent * 100.0) / 100.0
            ));
        }

        result.sort((a, b) -> Long.compare(b.getTotalDefects(), a.getTotalDefects()));

        return result.subList(0, Math.min(limit, result.size()));
    }
}
