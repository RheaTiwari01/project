package com.backend.project.util;

import com.backend.project.Entity.Event;

import java.time.Instant;

public class TestUtil {

    public static Event event(String id, long duration, int defects) {
        Event e = new Event();
        e.setEventId(id);
        e.setMachineId("M1");
        e.setFactoryId("F1");
        e.setLineId("L1");
        e.setDurationMs(duration);
        e.setDefectCount(defects);
        e.setEventTime(Instant.now());
        e.setReceivedTime(Instant.now());
        return e;
    }
}
