package com.example.core.services;

import com.example.common.exceptions.TypeNotFoundException;
import com.example.core.domain.models.taxonomic.TaxonomicInput;
import com.example.core.domain.models.taxonomic.TaxonomicTarget;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IJobExecutionService {
    JobExecution save(String jobName, TaxonomicInput inputData, TaxonomicTarget targetData, Long id) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, TypeNotFoundException, DuplicateJobException;

    List<JobExecution> findAll(Pageable pageable) throws TypeNotFoundException;
    JobParameters getJobParameters(String jobName);
}
