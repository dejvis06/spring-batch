package com.example.core.batch.readers;

import com.example.core.batch.listeners.ExportJobListener;
import com.example.core.domain.entities.Line;
import com.example.rest.ExportState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;

import java.util.Iterator;

public class ExportItemReader implements ItemReader<Line> {

    private static final Logger log = LoggerFactory.getLogger(ExportJobListener.class);

    private final Iterator<Line> items;

    public ExportItemReader(Iterator<Line> items) {
        this.items = items;
    }

    public Line read() {
        if (items.hasNext()) {
            Line line = items.next();
            log.info("Read line: {}", line);
            return line;
        }
        return null;
    }
}