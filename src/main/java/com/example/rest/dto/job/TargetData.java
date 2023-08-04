package com.example.rest.dto.job;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class TargetData {
    private final String domain;
    private final String environment;
    private final String taxonomicBackbone;
    private final List<String> rules;
}
