package com.example.rest.controllers;

import com.example.common.exceptions.ActionNotFoundException;
import com.example.common.exceptions.TypeNotFoundException;
import com.example.core.batch.listeners.ExportJobListener;
import com.example.core.batch.processors.ExportItemProcessor;
import com.example.core.batch.readers.ExportItemReader;
import com.example.core.batch.writters.ExportItemWritter;
import com.example.core.domain.entities.Line;
import com.example.core.domain.models.taxonomic.TaxonomicInput;
import com.example.core.domain.models.taxonomic.TaxonomicTarget;
import com.example.core.services.IJobExecutionService;
import com.example.core.services.Mapper;
import com.example.rest.ExportState;
import com.example.rest.dto.JobExecutionDTO;
import com.example.rest.dto.job.JobDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.example.core.services.Mapper.*;

@RestController
@RequestMapping("/jobexecutions")
@Slf4j
public class JobExecutionController {

    private final IJobExecutionService jobExecutionService;
    private final JobLauncher jobLauncher;
    private final JobOperator jobOperator;
    private final JobRegistry jobRegistry;
    private final JobRepository jobRepository;
    private final JobExecutionDao jobExecutionDao;
    private final TransactionManager transactionManager;
    private final JobInstanceDao jobInstanceDao;

    public JobExecutionController(IJobExecutionService jobExecutionService, JobLauncher jobLauncher, JobOperator jobOperator, JobRegistry jobRegistry, JobRepository jobRepository, JobExecutionDao jobExecutionDao, TransactionManager transactionManager, JobInstanceDao jobInstanceDao) {
        this.jobExecutionService = jobExecutionService;
        this.jobLauncher = jobLauncher;
        this.jobOperator = jobOperator;
        this.jobRegistry = jobRegistry;
        this.jobRepository = jobRepository;
        this.jobExecutionDao = jobExecutionDao;
        this.transactionManager = transactionManager;
        this.jobInstanceDao = jobInstanceDao;
    }

    @PostMapping
    @Operation(summary = "Save or update", description = "Send id only on update")
    public ResponseEntity<JobExecutionDTO> save(@RequestBody JobDTO jobDTO, @RequestParam(required = false) Long jobExecutionId) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobRestartException, TypeNotFoundException, DuplicateJobException {
        log.info("Received job with parameters: {}", jobDTO);

        TaxonomicInput taxonomicInput = buildTaxonomicInput(jobDTO.getInputData());
        TaxonomicTarget taxonomicTarget = buildTaxonomicTarget(jobDTO.getTargetData());

        JobExecution jobExecution = jobExecutionService.save(jobDTO.getJobName(), taxonomicInput, taxonomicTarget, jobExecutionId);
        return ResponseEntity.ok(Mapper.buildJobExecutionDTO(jobExecution));
    }

    @GetMapping
    @Operation(summary = "Find all by pagination")
    public Page<JobExecutionDTO> findAll(Pageable pageable) throws TypeNotFoundException {
        return new PageImpl<>(jobExecutionService.findAll(pageable)
                .stream()
                .map(type -> buildJobExecutionDTO((JobExecution) type))
                .collect(Collectors.toList()));
    }

    @Operation(summary = "Control job, start | stop | restart")
    @GetMapping("/control/{id}")
    public ResponseEntity<String> control(@RequestParam String fileName, @RequestParam Action action, @PathVariable Long id) throws ActionNotFoundException, NoSuchJobExecutionException, JobExecutionNotRunningException, JobInstanceAlreadyCompleteException, NoSuchJobException, JobParametersInvalidException, JobRestartException, JobExecutionAlreadyRunningException {

        BatchStatus status;
        switch (action) {
            case LAUNCH:
                Job job = jobRegistry.getJob(fileName);
                jobLauncher.run(job, jobExecutionService.getJobParameters(fileName));
                status = BatchStatus.STARTED;
                break;

            case STOP:
                jobOperator.stop(id);
                status = BatchStatus.STOPPED;
                break;

            case RESTART:
                jobOperator.restart(id);
                status = BatchStatus.STARTED;
                break;
            default:
                throw new ActionNotFoundException("Action not found!");
        }
        return ResponseEntity.ok(status.toString());
    }

    @Operation(summary = "export")
    @GetMapping(value = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> export() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, IOException, JobParametersInvalidException, JobRestartException, InterruptedException {
        return ResponseEntity.ok()
                .body(runExportJob());
    }

    private byte[] runExportJob() throws IOException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, InterruptedException {
        ExportState exportState = new ExportState(Arrays.asList(new Line("line 1"), new Line("line 2")));

        final JobParameters jobParameters = new JobParametersBuilder()
                .addDate("date", new Date())
                .toJobParameters();
        Job job = configureJob(exportState);

        this.jobRepository.createJobExecution("taxon_download", jobParameters);
        JobExecution execution = jobLauncher.run(job, jobParameters);
        return exportState.export();
    }

    private Job configureJob(ExportState exportState) throws IOException {
        Step step = new StepBuilder("taxon_download_step")
                .repository(jobRepository)
                .transactionManager((PlatformTransactionManager) transactionManager)
                .<Line, Line>chunk(1)
                .reader(new ExportItemReader(exportState))
                .processor(new ExportItemProcessor())
                .writer(new ExportItemWritter(exportState.getCsvWriter()))
                .build();

        return new JobBuilder("taxon_download")
                .repository(jobRepository)
                .listener(new ExportJobListener(exportState))
                .start(step)
                .build();
    }

    private enum Action {
        LAUNCH,
        STOP,
        RESTART
    }
}
