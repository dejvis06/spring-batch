package com.example.core.batch.processors;

import com.example.core.domain.entities.TaxonomicAssessment;
import com.example.core.domain.models.taxonomic.TaxonomicModel;
import com.example.core.domain.models.taxonomic.TaxonomicTarget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class TaxonomicProcessor implements ItemProcessor<TaxonomicModel, TaxonomicAssessment> {

    private final TaxonomicTarget taxonomicTarget;

    public TaxonomicProcessor(TaxonomicTarget taxonomicTarget) {
        this.taxonomicTarget = taxonomicTarget;
    }

    @Override
    public TaxonomicAssessment process(TaxonomicModel taxonomicModel) throws Exception {
        log.info("Received taxonomic model: {}", taxonomicModel);
        return null;
    }
}
