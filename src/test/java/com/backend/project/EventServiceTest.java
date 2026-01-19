package com.backend.project;

import com.backend.project.DTO.StatDto;
import com.backend.project.Entity.Event;
import com.backend.project.repository.EventRepo;
import com.backend.project.Service.EventService;
import com.backend.project.Service.StatService;
import com.backend.project.DTO.IngestDto;

import com.backend.project.util.TestUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest

class EventServiceTest {



        @Autowired
        private EventService eventService;

        @Autowired
        private StatService statService;

        @Autowired
        private EventRepo eventRepo;

        @BeforeEach
        void cleanDb() {
            eventRepo.deleteAll();
        }




        @Test
    void testDuplicateEventDeduped() {
        Event e1 = TestUtil.event("E1", 1000, 0);
        Event e2 = TestUtil.event("E1", 1000, 0);

        IngestDto res = eventService.ingest(List.of(e1, e2));

        assertEquals(1, res.getAccepted());
        assertEquals(1, res.getDeduped());
    }

    // 2️⃣ Newer receivedTime → update
    @Test
    void testUpdateWithNewerReceivedTime() {
        Event oldEvent = TestUtil.event("E2", 1000, 1);
        oldEvent.setReceivedTime(Instant.now().minusSeconds(100));

        Event newEvent = TestUtil.event("E2", 1500, 2);
        newEvent.setReceivedTime(Instant.now());

        eventService.ingest(List.of(oldEvent));
        IngestDto res = eventService.ingest(List.of(newEvent));

        assertEquals(1, res.getUpdated());
    }

    @Test
    void testOlderEventIgnored() {
        Event newEvent = TestUtil.event("E3", 1000, 1);
        newEvent.setReceivedTime(Instant.now());

        Event oldEvent = TestUtil.event("E3", 2000, 2);
        oldEvent.setReceivedTime(Instant.now().minusSeconds(100));

        eventService.ingest(List.of(newEvent));
        IngestDto res = eventService.ingest(List.of(oldEvent));

        assertEquals(0, res.getUpdated());
    }


    @Test
    void testInvalidDurationRejected() {
        Event bad = TestUtil.event("E4", -10, 1);

        IngestDto res = eventService.ingest(List.of(bad));

        assertEquals(1, res.getRejected());
    }

    @Test
    void testFutureEventRejected() {
        Event future = TestUtil.event("E5", 1000, 1);
        future.setEventTime(Instant.now().plusSeconds(5000));

        IngestDto res = eventService.ingest(List.of(future));

        assertEquals(1, res.getRejected());
    }

    @Test
    void testDefectMinusOneIgnored() {
        Event e = TestUtil.event("E6", 1000, -1);

        eventService.ingest(List.of(e));

        StatDto stats = statService.getStats(
                "M1",
                Instant.now().minusSeconds(3600),
                Instant.now()
        );

        assertEquals(0, stats.getDefectsCount());
    }


    @Test
    void testTimeBoundary() {
        Instant t = Instant.now();

        Event e = TestUtil.event("E7", 1000, 1);
        e.setEventTime(t);

        eventService.ingest(List.of(e));

        StatDto stats = statService.getStats("M1", t, t.plusSeconds(1));

        assertEquals(1, stats.getEventsCount());
    }


    @Test
    void testConcurrentIngestion() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        Runnable task = () -> {
            Event e = TestUtil.event(UUID.randomUUID().toString(), 1000, 1);
            eventService.ingest(List.of(e));
        };

        for (int i = 0; i < 20; i++) {
            executor.submit(task);
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        assertTrue(eventRepo.count() > 0);

    }
}
