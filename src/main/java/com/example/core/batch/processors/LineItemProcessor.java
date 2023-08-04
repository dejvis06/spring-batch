package com.example.core.batch.processors;

import com.example.core.domain.entities.Line;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class LineItemProcessor implements ItemProcessor<Line, Line> {

    private static final Logger log = LoggerFactory.getLogger(LineItemProcessor.class);

    private int count = 0;

    @Override
    public Line process(final Line line) throws Exception {
        log.info("Received line with text: {}", line.getText());
        log.info("Count: {}", ++count);

        Line transformedLine = new Line(line.getText().toUpperCase());
        Thread.sleep(4000);
        return transformedLine;
    }
}
