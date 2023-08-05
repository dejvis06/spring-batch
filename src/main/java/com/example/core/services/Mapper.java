package com.example.core.services;

import com.example.core.domain.models.taxonomic.TaxonomicInput;
import com.example.core.domain.models.taxonomic.TaxonomicTarget;
import com.example.rest.dto.JobExecutionDTO;
import com.example.rest.dto.job.InputData;
import com.example.rest.dto.job.TargetData;
import org.springframework.batch.core.JobExecution;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Mapper {

    public static JobExecutionDTO buildJobExecutionDTO(JobExecution jobExecution) {

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
                .fileName(jobExecution.getJobInstance().getJobName())
                .status(jobExecution.getStatus().toString())
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

    public static TaxonomicInput buildTaxonomicInput(InputData inputData) {
        return TaxonomicInput.builder()
                .dataset(inputData.getDataset())
                .csv(inputData.getCsv())
                .rowDelimiter(inputData.getRowDelimiter())
                .columnDelimiter(inputData.getColumnDelimiter())
                .build();
    }

    public static TaxonomicTarget buildTaxonomicTarget(TargetData targetData) {
        return TaxonomicTarget.builder()
                .domain(targetData.getDomain())
                .environment(targetData.getEnvironment())
                .taxonomicBackbone(targetData.getTaxonomicBackbone())
                .rules(targetData.getRules())
                .build();
    }
}
