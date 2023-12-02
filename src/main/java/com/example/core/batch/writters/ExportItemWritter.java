package com.example.core.batch.writters;

import com.example.core.domain.entities.Line;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class ExportItemWritter implements ItemWriter<Line> {

    private static final Logger log = LoggerFactory.getLogger(ExportItemWritter.class);

    private final CSVWriter csvWriter;

    public ExportItemWritter(CSVWriter csvWriter) {
        this.csvWriter = csvWriter;
    }

    @Override
    public void write(List<? extends Line> list) {
        if (!CollectionUtils.isEmpty(list)) {
            log.info("Writting line: {}", list);
            csvWriter.writeNext(new String[]{list.get(0).toString()});
        }
    }
}
