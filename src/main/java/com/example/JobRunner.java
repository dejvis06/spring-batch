package com.example;

import com.example.core.batch.config.BatchConfiguration;
import com.example.core.batch.listeners.ExportJobListener;
import com.example.core.batch.processors.ExportItemProcessor;
import com.example.core.batch.readers.ExportItemReader;
import com.example.core.batch.writters.ExportItemWritter;
import com.example.core.domain.entities.Line;
import com.example.rest.ExportState;
import com.example.rest.controllers.JobExecutionController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

@Component
public class JobRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BatchConfiguration.class);

    private final JobRepository jobRepository;
    private final JobLauncher jobLauncher;
    private final Job importLineJob;
    private final JobOperator jobOperator;
    private final JobExecutionDao jobExecutionDao;
    private final JobInstanceDao jobInstanceDao;
    private final JobExecutionController jobExecutionController;
    private final TransactionManager transactionManager;
    private final JobParameters jobParameters = new JobParametersBuilder()
            .addDate("date", new Date())
            .addString("test_attribute", "test_value")
            .toJobParameters();

    public JobRunner(JobRepository jobRepository, JobLauncher jobLauncher, Job importLineJob, JobOperator jobOperator, JobExecutionDao jobExecutionDao, JobInstanceDao jobInstanceDao, JobExecutionController jobExecutionController, TransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.jobLauncher = jobLauncher;
        this.importLineJob = importLineJob;
        this.jobOperator = jobOperator;
        this.jobExecutionDao = jobExecutionDao;
        this.jobInstanceDao = jobInstanceDao;
        this.jobExecutionController = jobExecutionController;
        this.transactionManager = transactionManager;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // runCustomJob();
        // runDefaultJob();
    }

    private void runCustomJob() throws IOException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        ExportState exportState = new ExportState(Arrays.asList(new Line("line 1"), new Line("line 2")));
        Step step = new StepBuilder("taxon_download_step")
                .repository(jobRepository)
                .transactionManager((PlatformTransactionManager) transactionManager)
                .<Line, Line>chunk(1)
                .reader(new ExportItemReader(exportState.getItems()))
                .processor(new ExportItemProcessor())
                .writer(new ExportItemWritter(exportState.getCsvWriter()))
                .build();

        // TODO add HttpServletRequest
        Job job = new JobBuilder("taxon_download")
                .repository(jobRepository)
                .listener(new ExportJobListener(exportState))
                .start(step)
                .build();
        this.jobRepository.createJobExecution("taxon_download", jobParameters);
        jobLauncher.run(job, jobParameters);
        jobInstanceDao.getJobNames().forEach(jobName -> {
            log.info("Found job name: {}", jobName);

            JobInstance jobInstance = jobInstanceDao.getJobInstance(jobName, jobParameters);
            log.info("Job instance: {}", jobInstance);

            log.info("Post Construct job executions: {}", jobExecutionDao.findJobExecutions(jobInstance));
        });
    }

    private void runDefaultJob() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        this.jobRepository.createJobExecution("importLineJob", jobParameters);
        jobLauncher.run(importLineJob, jobParameters);

        jobInstanceDao.getJobNames().forEach(jobName -> {
            log.info("Found job name: {}", jobName);

            JobInstance jobInstance = jobInstanceDao.getJobInstance(jobName, jobParameters);
            log.info("Job instance: {}", jobInstance);

            log.info("Post Construct job executions: {}", jobExecutionDao.findJobExecutions(jobInstance));
        });
    }
}
