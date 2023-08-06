package com.example.rest.dto.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class InputData {
    private final String dataset;
    private final String csv;
    private final String rowDelimiter;
    private final String columnDelimiter;
}
