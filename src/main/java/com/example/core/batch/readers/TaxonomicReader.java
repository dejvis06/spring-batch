package com.example.core.batch.readers;

import com.example.core.domain.models.taxonomic.TaxonomicInput;
import com.example.core.domain.models.taxonomic.TaxonomicModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

@Slf4j
public class TaxonomicReader implements ItemReader<TaxonomicModel> {

    private final TaxonomicInput inputData;

    public TaxonomicReader(TaxonomicInput inputData) {
        this.inputData = inputData;
    }

    @Override
    public TaxonomicModel read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        log.info("Reading from input: {}", inputData);
        return null;
    }
}
