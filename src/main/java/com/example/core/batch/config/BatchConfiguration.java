package com.example.core.batch.config;

import com.example.core.services.impl.CustomJobLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.JdbcJobExecutionDao;
import org.springframework.batch.core.repository.dao.JdbcJobInstanceDao;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.incrementer.HsqlMaxValueIncrementer;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    private static final Logger log = LoggerFactory.getLogger(BatchConfiguration.class);

    @Bean
    public DataSource batchDataSource() {
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL)
                .addScript("/schema-hsqldb.sql")
                .generateUniqueName(true).build();
    }

    @Bean
    public JdbcTemplate jdbcTemplateDatasource(DataSource batchDataSource) {
        return new JdbcTemplate(batchDataSource);
    }

    @Bean
    public JdbcOperations jdbcTemplate(JdbcTemplate jdbcTemplateDatasource) {
        return jdbcTemplateDatasource;
    }

    @Bean
    public JobExecutionDao jobExecutionDao(JdbcOperations jdbcTemplate) {
        JdbcJobExecutionDao jdbcJobExecutionDao = new JdbcJobExecutionDao();
        jdbcJobExecutionDao.setJdbcTemplate(jdbcTemplate);
        jdbcJobExecutionDao.setJobExecutionIncrementer(new HsqlMaxValueIncrementer());
        return jdbcJobExecutionDao;
    }

    @Bean
    public JobInstanceDao jobInstanceDao(JdbcOperations jdbcTemplate) {
        JdbcJobInstanceDao jobInstanceDao = new JdbcJobInstanceDao();
        jobInstanceDao.setJdbcTemplate(jdbcTemplate);
        jobInstanceDao.setJobIncrementer(new HsqlMaxValueIncrementer());
        return jobInstanceDao;
    }

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        CustomJobLauncher jobLauncher = new CustomJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Bean
    public JobRegistry jobRegistry() {
        return new MapJobRegistry();
    }

    /**
     * All injected dependencies for this bean are provided by the @EnableBatchProcessing
     * infrastructure out of the box.
     */
    @Bean
    public JobOperator jobOperator(JobExplorer jobExplorer,
                                   JobRepository jobRepository,
                                   JobRegistry jobRegistry,
                                   JobLauncher jobLauncher) {

        SimpleJobOperator jobOperator = new SimpleJobOperator();
        jobOperator.setJobExplorer(jobExplorer);
        jobOperator.setJobRepository(jobRepository);
        jobOperator.setJobRegistry(jobRegistry);
        jobOperator.setJobLauncher(jobLauncher);
        return jobOperator;
    }
}
