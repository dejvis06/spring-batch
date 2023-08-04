package com.example.core.batch.readers;

import com.example.core.domain.entities.Line;
import org.springframework.batch.item.*;

import java.util.Arrays;
import java.util.List;

public class CustomItemReader implements ItemReader<Line> {

    private final List<String> items;
    private boolean stopped = false;
    int currentIndex = 0;
    //private static final Long CURRENT_INDEX = "current.index";

    public CustomItemReader() {
        this.items = Arrays.asList("Buenos Aires", "Córdoba", "La Plata");
    }

    public Line read() throws Exception, UnexpectedInputException,
            ParseException, NonTransientResourceException {

        if (currentIndex < items.size() && !stopped) {
            return new Line(items.get(currentIndex++));
        }

        return null;
    }

    public void stop() {
        stopped = true;
    }

    public void start() {
        stopped = false;
    }
}

