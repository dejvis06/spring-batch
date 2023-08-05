package com.example.core.services.impl;

import com.example.common.exceptions.TypeNotFoundException;
import com.example.core.batch.listeners.TaxonomicListener;
import com.example.core.batch.processors.TaxonomicProcessor;
import com.example.core.batch.readers.TaxonomicReader;
import com.example.core.batch.writters.TaxonomicWritter;
import com.example.core.domain.entities.TaxonomicAssessment;
import com.example.core.domain.models.taxonomic.TaxonomicInput;
import com.example.core.domain.models.taxonomic.TaxonomicModel;
import com.example.core.domain.models.taxonomic.TaxonomicTarget;
import com.example.core.domain.repositories.CustomRepositoryFactory;
import com.example.core.services.IJobExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

import java.util.List;

import static com.example.core.domain.repositories.CustomRepositoryFactory.Type.JOB_EXECUTION;

@Service
@Slf4j
public class JobExecutionServiceImpl implements IJobExecutionService {

    private final TransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final JobInstanceDao jobInstanceDao;
    private final JobExecutionDao jobExecutionDao;
    private final GenericApplicationContext beanFactory;
    private final CustomRepositoryFactory customRepositoryFactory;

    public JobExecutionServiceImpl(TransactionManager transactionManager, JobRepository jobRepository, JobInstanceDao jobInstanceDao, JobExecutionDao jobExecutionDao, GenericApplicationContext beanFactory, CustomRepositoryFactory customRepositoryFactory) {
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
        this.jobInstanceDao = jobInstanceDao;
        this.jobExecutionDao = jobExecutionDao;
        this.beanFactory = beanFactory;
        this.customRepositoryFactory = customRepositoryFactory;
    }

    @Override
    public JobExecution save(String jobName, TaxonomicInput inputData, TaxonomicTarget targetData) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        TaxonomicReader taxonomicReader = new TaxonomicReader(inputData);
        TaxonomicProcessor taxonomicProcessor = new TaxonomicProcessor(targetData);
        TaxonomicWritter taxonomicWritter = new TaxonomicWritter();

        Step step = new StepBuilder(jobName + "_step")
                .repository(jobRepository)
                .transactionManager((PlatformTransactionManager) transactionManager)
                .<TaxonomicModel, TaxonomicAssessment>chunk(1)
                .reader(taxonomicReader)
                .processor(taxonomicProcessor)
                .writer(taxonomicWritter)
                .build();

        TaxonomicListener taxonomicListener = new TaxonomicListener();

        Job job = new JobBuilder(jobName)
                .repository(jobRepository)
                .listener(taxonomicListener)
                .start(step)
                .build();

        beanFactory.registerBean(jobName, Job.class, () -> job);
        Job verificationObject = beanFactory.getBean(jobName, Job.class);
        log.info("Registered job bean in the application context: {}", verificationObject);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("file_name", jobName)
                .toJobParameters();

        JobExecution jobExecution = jobRepository.createJobExecution(jobName, jobParameters);
        log.info("Created job instance: {}", jobInstanceDao.getJobInstance(jobExecution));
        log.info("Created job execution: {}", jobExecution);
        return jobExecution;
    }

    @Override
    public List<JobExecution> findAll(Pageable pageable) throws TypeNotFoundException {
        return customRepositoryFactory.getRepository(JOB_EXECUTION).findAll(pageable);
    }
}