package com.example.rest.controllers;

import com.example.common.exceptions.ActionNotFoundException;
import com.example.common.exceptions.TypeNotFoundException;
import com.example.core.domain.models.taxonomic.TaxonomicInput;
import com.example.core.domain.models.taxonomic.TaxonomicTarget;
import com.example.core.services.IJobExecutionService;
import com.example.core.services.Mapper;
import com.example.rest.dto.JobExecutionDTO;
import com.example.rest.dto.job.JobDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    public JobExecutionController(IJobExecutionService jobExecutionService, JobLauncher jobLauncher, JobOperator jobOperator, JobRegistry jobRegistry) {
        this.jobExecutionService = jobExecutionService;
        this.jobLauncher = jobLauncher;
        this.jobOperator = jobOperator;
        this.jobRegistry = jobRegistry;
    }

    @PostMapping
    public ResponseEntity<JobExecutionDTO> save(@RequestBody JobDTO jobDTO, @RequestParam Long jobExecutionId) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobRestartException, TypeNotFoundException, DuplicateJobException {
        log.info("Received job with parameters: {}", jobDTO);

        TaxonomicInput taxonomicInput = buildTaxonomicInput(jobDTO.getInputData());
        TaxonomicTarget taxonomicTarget = buildTaxonomicTarget(jobDTO.getTargetData());

        JobExecution jobExecution = jobExecutionService.save(jobDTO.getJobName(), taxonomicInput, taxonomicTarget, jobExecutionId);
        return ResponseEntity.ok(Mapper.buildJobExecutionDTO(jobExecution));
    }

    @GetMapping
    public Page<JobExecutionDTO> findAll(Pageable pageable) throws TypeNotFoundException {
        return new PageImpl<>(jobExecutionService.findAll(pageable)
                .stream()
                .map(type -> buildJobExecutionDTO((JobExecution) type))
                .collect(Collectors.toList()));
    }

    @GetMapping
    public ResponseEntity<String> control(@RequestParam String fileName, @RequestParam String action, @PathVariable Long id) throws ActionNotFoundException, NoSuchJobExecutionException, JobExecutionNotRunningException, JobInstanceAlreadyCompleteException, NoSuchJobException, JobParametersInvalidException, JobRestartException, JobExecutionAlreadyRunningException {

        BatchStatus status;
        switch (Action.valueOf(action)) {
            case LAUNCH:
                Job job = jobRegistry.getJob(fileName);
                JobParameters jobParameters = new JobParametersBuilder()
                        .addString("file_name", fileName)
                        .toJobParameters();
                jobLauncher.run(job, jobParameters);
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

    public enum Action {
        LAUNCH,
        STOP,
        RESTART
    }
}
