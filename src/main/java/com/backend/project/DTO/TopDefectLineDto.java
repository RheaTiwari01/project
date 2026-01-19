package com.backend.project.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopDefectLineDto {
    private String lineId;
    private long totalDefects;
    private long eventCount;
    private double defectsPercent;
}
