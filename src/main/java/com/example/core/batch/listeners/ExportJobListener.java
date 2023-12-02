package com.example.core.batch.listeners;

import com.example.rest.ExportState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.io.IOException;

public class ExportJobListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(ExportJobListener.class);
    private static final String CSV_FILE_PATH
            = "./result.csv";
    private final ExportState exportState;

    public ExportJobListener(ExportState exportState) throws IOException {
        this.exportState = exportState;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("After Job: {}", jobExecution);
        try {
            log.info("Closing csv writter...");
            exportState.getCsvWriter().close();
            exportState.finished();
        } catch (Exception e) {
            log.error("Error closing csv writter: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        /*try {
            this.response.getOutputStream().flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }
}
