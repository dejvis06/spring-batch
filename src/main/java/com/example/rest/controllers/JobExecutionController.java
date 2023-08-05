package com.example.rest.controllers;

import com.example.core.domain.models.taxonomic.TaxonomicInput;
import com.example.core.domain.models.taxonomic.TaxonomicTarget;
import com.example.core.services.IJobExecutionService;
import com.example.core.services.Mapper;
import com.example.rest.dto.JobExecutionDTO;
import com.example.rest.dto.job.JobDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
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

    public JobExecutionController(IJobExecutionService jobExecutionService) {
        this.jobExecutionService = jobExecutionService;
    }

    @PostMapping
    public ResponseEntity<JobExecutionDTO> save(@RequestBody JobDTO jobDTO) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobRestartException {
        log.info("Creating job with parameters: {}", jobDTO);

        TaxonomicInput taxonomicInput = buildTaxonomicInput(jobDTO.getInputData());
        TaxonomicTarget taxonomicTarget = buildTaxonomicTarget(jobDTO.getTargetData());

        JobExecution jobExecution = jobExecutionService.save(jobDTO.getJobName(), taxonomicInput, taxonomicTarget);
        return ResponseEntity.ok(Mapper.buildJobExecutionDTO(jobExecution));
    }

    @GetMapping
    public Page<JobExecutionDTO> findAll(Pageable pageable) throws Exception {
        return new PageImpl<>(jobExecutionService.findAll(pageable)
                .stream()
                .map(type -> buildJobExecutionDTO((JobExecution) type))
                .collect(Collectors.toList()));
    }
}
