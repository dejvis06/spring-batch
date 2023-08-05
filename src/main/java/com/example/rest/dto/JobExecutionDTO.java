package com.example.rest.dto;

import lombok.Builder;
import lombok.ToString;

import java.time.LocalDateTime;

@Builder
@ToString
public class JobExecutionDTO {

    private Long id;
    private String fileName;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
