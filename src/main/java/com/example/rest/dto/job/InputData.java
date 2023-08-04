package com.example.rest.dto.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class InputData {
    private final String dataset;
    private final String csv;
    private final String rowDelimiter;
    private final String columnDelimiter;
}
