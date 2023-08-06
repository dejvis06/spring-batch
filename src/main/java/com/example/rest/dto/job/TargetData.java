package com.example.rest.dto.job;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
@ToString
public class TargetData {
    private final String domain;
    private final String environment;
    private final String taxonomicBackbone;
    private final List<String> rules;
}
