package com.example.core.batch.writters;

import com.example.core.domain.entities.TaxonomicAssessment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@Slf4j
public class TaxonomicWritter implements ItemWriter<TaxonomicAssessment> {

    @Override
    public void write(List<? extends TaxonomicAssessment> list) throws Exception {
        log.info("Received taxonomic assessments: {}", list);
    }
}
