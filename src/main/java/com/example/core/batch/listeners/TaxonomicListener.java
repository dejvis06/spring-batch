package com.example.core.batch.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

@Slf4j
public class TaxonomicListener implements JobExecutionListener {
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Starting job: {}", jobExecution);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        BatchStatus status = jobExecution.getStatus();

        if (status.equals(BatchStatus.COMPLETED)) {
            log.info("Successfully completed job: {}", jobExecution);
        } else if (status.equals(BatchStatus.STOPPED)) {
            log.info("Successfully stopped job: {}", jobExecution);
        }
    }
}
