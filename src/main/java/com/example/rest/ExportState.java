package com.example.rest;

import com.example.core.domain.entities.Line;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;

public class ExportState {
    private static final String CSV_FILE_PATH
            = "./result.csv";
    private final Iterator<Line> items;
    private File file;
    private final CSVWriter csvWriter;
    private boolean finished;

    public ExportState(List<Line> items) throws IOException {
        this.items = items.iterator();
        this.file = new File(CSV_FILE_PATH);
        this.csvWriter = new CSVWriter(new FileWriter(this.file), ';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
        finished = false;
    }

    public Iterator<Line> getItems() {
        return items;
    }

    public CSVWriter getCsvWriter() {
        return csvWriter;
    }

    public void finished() {
        this.finished = !finished;
    }

    public boolean isFinished() {
        return finished;
    }

    public byte[] export() throws IOException {
        return Files.readAllBytes(file.toPath());
    }

}
