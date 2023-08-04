package com.example.core.domain.models.taxonomic;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class TaxonomicTarget {

    private final String domain;
    private final String environment;
    private final String taxonomicBackbone;
    private final List<String> rules;

    public TaxonomicTarget(String domain, String environment, String taxonomicBackbone, List<String> rules) {
        this.domain = domain;
        this.environment = environment;
        this.taxonomicBackbone = taxonomicBackbone;
        this.rules = rules;
    }
}
