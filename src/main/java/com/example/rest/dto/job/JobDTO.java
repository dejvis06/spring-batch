package com.example.rest.dto.job;

import lombok.*;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class JobDTO {

    private final String jobName;
    private final InputData inputData;
    private final TargetData targetData;
}
