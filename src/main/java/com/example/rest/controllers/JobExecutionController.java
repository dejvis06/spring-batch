package com.example.rest.controllers;

import com.example.core.batch.listeners.TaxonomicListener;
import com.example.core.batch.processors.TaxonomicProcessor;
import com.example.core.batch.readers.TaxonomicReader;
import com.example.core.batch.writters.TaxonomicWritter;
import com.example.core.domain.entities.TaxonomicAssessment;
import com.example.core.domain.models.taxonomic.TaxonomicInput;
import com.example.core.domain.models.taxonomic.TaxonomicModel;
import com.example.core.domain.models.taxonomic.TaxonomicTarget;
import com.example.core.domain.repositories.CustomRepositoryFactory;
import com.example.rest.dto.JobExecutionDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.core.domain.repositories.CustomRepositoryFactory.Type.JOB_EXECUTION;

@RestController
@RequestMapping("/jobexecutions")
@Slf4j
public class JobExecutionController {

    private final TransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final JobInstanceDao jobInstanceDao;
    private final JobExecutionDao jobExecutionDao;
    private final GenericApplicationContext beanFactory;
    private final CustomRepositoryFactory customRepositoryFactory;

    public JobExecutionController(TransactionManager transactionManager, JobRepository jobRepository, JobInstanceDao jobInstanceDao, JobExecutionDao jobExecutionDao, GenericApplicationContext beanFactory, CustomRepositoryFactory customRepositoryFactory) {
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
        this.jobInstanceDao = jobInstanceDao;
        this.jobExecutionDao = jobExecutionDao;
        this.beanFactory = beanFactory;
        this.customRepositoryFactory = customRepositoryFactory;
    }

    @PostMapping
    public ResponseEntity<HttpStatus> save(@RequestBody JobDTO jobDTO) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobRestartException {
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
        Job verificationObject = beanFactory.getBean(jobDTO.getJobName(), Job.class);
        log.info("Registered job bean in the application context: {}", verificationObject);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("file_name", jobDTO.getJobName())
                .toJobParameters();

        JobExecution jobExecution = jobRepository.createJobExecution(jobDTO.getJobName(), jobParameters);
        log.info("Created job instance: {}", jobInstanceDao.getJobInstance(jobExecution));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public Page<JobExecutionDTO> findAll(Pageable pageable) throws Exception {
        List<JobExecutionDTO> jobExecutionDTOS = (List<JobExecutionDTO>) customRepositoryFactory.getRepository(JOB_EXECUTION).findAll(pageable)
                .stream()
                .map(type -> buildJobExecutionDTO((JobExecution) type))
                .collect(Collectors.toList());

        return new PageImpl<>(jobExecutionDTOS);
    }

    private static JobExecutionDTO buildJobExecutionDTO(JobExecution type) {
        JobExecution jobExecution = type;

        LocalDateTime startTime = null;
        if (jobExecution.getStartTime() != null) {
            startTime = LocalDateTime.ofInstant(jobExecution.getStartTime().toInstant(),
                    ZoneId.systemDefault());
        }
        LocalDateTime endTime = null;
        if (jobExecution.getEndTime() != null) {
            endTime = LocalDateTime.ofInstant(jobExecution.getEndTime().toInstant(),
                    ZoneId.systemDefault());
        }

        return JobExecutionDTO.builder()
                .id(jobExecution.getId())
                .fileName(type.getJobInstance().getJobName())
                .status(jobExecution.getStatus().toString())
                .startTime(startTime)
                .endTime(endTime)
                .build();
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
