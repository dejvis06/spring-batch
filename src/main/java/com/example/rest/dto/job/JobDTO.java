package com.example.rest.dto.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@AllArgsConstructor
@ToString
public class JobDTO {

    private final String jobName;
    private final InputData inputData;
    private final TargetData targetData;
}
