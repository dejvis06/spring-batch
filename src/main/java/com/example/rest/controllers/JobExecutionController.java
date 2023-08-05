package com.example.rest.controllers;

import com.example.core.batch.listeners.TaxonomicListener;
import com.example.core.batch.processors.TaxonomicProcessor;
import com.example.core.batch.readers.TaxonomicReader;
import com.example.core.batch.writters.TaxonomicWritter;
import com.example.core.domain.entities.TaxonomicAssessment;
import com.example.core.domain.models.taxonomic.TaxonomicInput;
import com.example.core.domain.models.taxonomic.TaxonomicModel;
import com.example.core.domain.models.taxonomic.TaxonomicTarget;
import com.example.rest.dto.job.InputData;
import com.example.rest.dto.job.JobDTO;
import com.example.rest.dto.job.TargetData;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobexecutions")
@Slf4j
public class JobExecutionController {

    private final TransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final JobInstanceDao jobInstanceDao;
    private final JobExecutionDao jobExecutionDao;
    private final GenericApplicationContext beanFactory;

    public JobExecutionController(TransactionManager transactionManager, JobRepository jobRepository, JobInstanceDao jobInstanceDao, JobExecutionDao jobExecutionDao, GenericApplicationContext beanFactory) {
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
        this.jobInstanceDao = jobInstanceDao;
        this.jobExecutionDao = jobExecutionDao;
        this.beanFactory = beanFactory;
    }

    @PostMapping
    public ResponseEntity<HttpStatus> create(@RequestBody JobDTO jobDTO) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobRestartException {
        log.info("Creating job with parameters: {}", jobDTO);

        InputData inputData = jobDTO.getInputData();
        TargetData targetData = jobDTO.getTargetData();

        TaxonomicReader taxonomicReader = new TaxonomicReader(buildTaxonomicInput(inputData));
        TaxonomicProcessor taxonomicProcessor = new TaxonomicProcessor(buildTaxonomicTarget(targetData));
        TaxonomicWritter taxonomicWritter = new TaxonomicWritter();

        Step step = new StepBuilder(jobDTO.getJobName() + "_step")
                .repository(jobRepository)
                .transactionManager((PlatformTransactionManager) transactionManager)
                .<TaxonomicModel, TaxonomicAssessment>chunk(1)
                .reader(taxonomicReader)
                .processor(taxonomicProcessor)
                .writer(taxonomicWritter)
                .build();

        TaxonomicListener taxonomicListener = new TaxonomicListener();

        Job job = new JobBuilder(jobDTO.getJobName())
                .repository(jobRepository)
                .listener(taxonomicListener)
                .start(step)
                .build();

        beanFactory.registerBean(jobDTO.getJobName(), Job.class, () -> job);
        Job temp = beanFactory.getBean(jobDTO.getJobName(), Job.class);
        log.info("Registered job bean in the application context: {}", temp);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("file_name", jobDTO.getJobName())
                .toJobParameters();

        JobExecution jobExecution = jobRepository.createJobExecution(jobDTO.getJobName(), jobParameters);
        log.info("Created job instance: {}", jobInstanceDao.getJobInstance(jobExecution));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private TaxonomicInput buildTaxonomicInput(InputData inputData) {
        return TaxonomicInput.builder()
                .dataset(inputData.getDataset())
                .csv(inputData.getCsv())
                .rowDelimiter(inputData.getRowDelimiter())
                .columnDelimiter(inputData.getColumnDelimiter())
                .build();
    }

    private TaxonomicTarget buildTaxonomicTarget(TargetData targetData) {
        return TaxonomicTarget.builder()
                .domain(targetData.getDomain())
                .environment(targetData.getEnvironment())
                .taxonomicBackbone(targetData.getTaxonomicBackbone())
                .rules(targetData.getRules())
                .build();
    }

}
