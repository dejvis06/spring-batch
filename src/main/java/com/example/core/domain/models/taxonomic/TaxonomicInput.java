package com.example.core.domain.models.taxonomic;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@Builder
@ToString
public class TaxonomicInput {
    private final String dataset;
    private final String csv;
    private final String rowDelimiter;
    private final String columnDelimiter;

    public TaxonomicInput(String dataset, String csv, String rowDelimiter, String columnDelimiter) {
        this.dataset = dataset;
        this.csv = csv;
        this.rowDelimiter = rowDelimiter;
        this.columnDelimiter = columnDelimiter;
    }
}
