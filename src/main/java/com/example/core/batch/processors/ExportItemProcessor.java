package com.example.core.batch.processors;

import com.example.core.domain.entities.Line;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class ExportItemProcessor implements ItemProcessor<Line, Line> {

    private static final Logger log = LoggerFactory.getLogger(ExportItemProcessor.class);

    @Override
    public Line process(Line line) throws Exception {
        // TODO: process metadata field
        log.info("Processing line: {}", line);
        return line;
    }
}
