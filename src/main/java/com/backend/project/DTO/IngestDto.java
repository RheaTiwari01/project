package com.backend.project.DTO;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
public class IngestDto {
    private int accepted;
    private int deduped;
    private int updated;
    private int rejected;
    private List<Rejection> rejections;
}
