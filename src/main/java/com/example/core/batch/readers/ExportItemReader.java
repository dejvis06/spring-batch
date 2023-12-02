package com.example.core.batch.readers;

import com.example.core.batch.listeners.ExportJobListener;
import com.example.core.domain.entities.Line;
import com.example.rest.ExportState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;

public class ExportItemReader implements ItemReader<Line> {

    private static final Logger log = LoggerFactory.getLogger(ExportJobListener.class);

    private ExportState exportState;

    public ExportItemReader(ExportState exportState) {
        this.exportState = exportState;
    }

    public Line read() {
        if (exportState.getItems().hasNext()) {
            Line line = exportState.getItems().next();
            log.info("Read line: {}", line);
            return line;
        }
        return null;
    }
}