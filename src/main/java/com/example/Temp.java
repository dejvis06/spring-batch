package com.example;

import com.example.core.batch.config.BatchConfiguration;
import com.example.rest.controllers.JobExecutionController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

@Component
@EnableScheduling
public class Temp {

    private static final Logger log = LoggerFactory.getLogger(BatchConfiguration.class);

    private final JobRepository jobRepository;
    private final JobLauncher jobLauncher;
    private final Job importLineJob;
    private final JobOperator jobOperator;
    private final JobExecutionDao jobExecutionDao;
    private final JobInstanceDao jobInstanceDao;
    private final JobExecutionController jobExecutionController;
    private final JobParameters jobParameters = new JobParametersBuilder()
            .addDate("date", new Date())
            .addString("test_attribute", "test_value")
            .toJobParameters();

    public Temp(JobRepository jobRepository, JobLauncher jobLauncher, Job importLineJob, JobOperator jobOperator, JobExecutionDao jobExecutionDao, JobInstanceDao jobInstanceDao, JobExecutionController jobExecutionController) {
        this.jobRepository = jobRepository;
        this.jobLauncher = jobLauncher;
        this.importLineJob = importLineJob;
        this.jobOperator = jobOperator;
        this.jobExecutionDao = jobExecutionDao;
        this.jobInstanceDao = jobInstanceDao;
        this.jobExecutionController = jobExecutionController;
    }

    @PostConstruct
    void temp() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, InterruptedException {
        jobLauncher.run(importLineJob, jobParameters);

        jobInstanceDao.getJobNames().forEach(jobName -> {
            log.info("Found job name: {}", jobName);

            JobInstance jobInstance = jobInstanceDao.getJobInstance(jobName, jobParameters);
            log.info("Job instance: {}", jobInstance);

            log.info("Post Construct job executions: {}", jobExecutionDao.findJobExecutions(jobInstance));
        });
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 2000)
    public void scheduleFixedDelayTask() {
        log.info("Started scheduler");

        try {

            log.info("All running executions: {}", jobOperator.getRunningExecutions("importLineJob"));

            jobOperator.getRunningExecutions("importLineJob").forEach(runningExec -> {
                log.info("Running execution: {}", runningExec);
                try {
                    log.info("Stopping execution");
                    jobOperator.stop(runningExec);
                    log.info("Sleeping for 10 seconds...");
                    Thread.sleep(10000);
                    log.info("Finished sleeping");

                    jobOperator.restart(runningExec);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
