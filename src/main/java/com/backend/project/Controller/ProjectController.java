package com.backend.project.Controller;


import com.backend.project.DTO.IngestDto;
import com.backend.project.DTO.StatDto;
import com.backend.project.DTO.TopDefectLineDto;
import com.backend.project.Entity.Event;
import com.backend.project.Service.EventService;
import com.backend.project.Service.StatService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
public class ProjectController {

    private final EventService eventService;
    private final StatService statService;

    public ProjectController(EventService eventService,
                             StatService statService) {
        this.eventService = eventService;
        this.statService = statService;
    }

    // =========================================================
    // 1. INGEST EVENTS
    // POST /events/batch
    // =========================================================
    @PostMapping("/events/batch")
    public IngestDto ingest(@RequestBody List<Event> events) {
        return eventService.ingest(events);
    }

    // =========================================================
    // 2. QUERY STATS
    // GET /stats?machineId=...&start=...&end=...
    // =========================================================
    @GetMapping("/stats")
    public StatDto getStats(
            @RequestParam String machineId,
            @RequestParam Instant start,
            @RequestParam Instant end
    ) {
        return statService.getStats(machineId, start, end);
    }

    // =========================================================
    // 3. TOP DEFECT LINES
    // GET /stats/top-defect-lines
    // =========================================================
    @GetMapping("/stats/top-defect-lines")
    public List<TopDefectLineDto> getTopDefectLines(
            @RequestParam String factoryId,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return statService.getTopDefectLines(factoryId, from, to, limit);
    }
}

