package com.backend.project.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Rejection {
    private String eventId;
    private String reason;
}
