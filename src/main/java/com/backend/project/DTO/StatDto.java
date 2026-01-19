package com.backend.project.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatDto {
    private String machineId;
    private String start;
    private String end;
    private long eventsCount;
    private long defectsCount;
    private double avgDefectRate;
    private String status;
}





