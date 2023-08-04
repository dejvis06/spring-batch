package com.example.core.batch.listeners;

import com.example.core.domain.entities.Line;
import com.example.core.batch.readers.CustomItemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    private final JdbcOperations jdbcTemplate;
    private final CustomItemReader customItemReader;

    @Autowired
    public JobCompletionNotificationListener(JdbcOperations jdbcTemplate, CustomItemReader customItemReader) {
        this.jdbcTemplate = jdbcTemplate;
        this.customItemReader = customItemReader;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // jobExecution.setStatus(BatchStatus.STARTING);
        log.info("Before job log: {}", jobExecution);
        log.info("Job Status: {}", jobExecution.getStatus());
        log.info("Job instance: {}", jobExecution.getJobInstance());
        log.info("Job params: {}", jobExecution.getJobParameters());
        customItemReader.start();
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        log.info("Job Status: {}", jobExecution.getStatus());
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("!!! JOB FINISHED! Time to verify the results");

            jdbcTemplate.query("SELECT text FROM line",
                    (rs, row) -> new Line(rs.getString(1))
            ).forEach(line -> log.info("Found <{{}}> in the database.", line));
        } else if (jobExecution.getStatus() == BatchStatus.STOPPED) {
            customItemReader.stop();
        }
    }
}
