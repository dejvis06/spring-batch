package com.example.core.domain.entities;

public class Line {

    private String text;

    public Line() {
    }

    public Line(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Line{" +
                "text='" + text + '\'' +
                '}';
    }
}
