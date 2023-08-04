package com.example.core.batch.config;

import com.example.core.batch.readers.CustomItemReader;
import com.example.core.batch.listeners.JobCompletionNotificationListener;
import com.example.core.domain.entities.Line;
import com.example.core.batch.processors.LineItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;

@Configuration
public class JobConfiguration {


    /**
     * Necessary to register jobs
     */

    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry mapJobRegistry) {
        JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
        postProcessor.setJobRegistry(mapJobRegistry);
        return postProcessor;
    }

    @Bean
    public Job importLineJob(JobRepository jobRepository, JobCompletionNotificationListener listener, Step step) {

        return new JobBuilder("importLineJob")
                .repository(jobRepository)
                .listener(listener)
                .start(step)
                .build();
    }

    @Bean
    public CustomItemReader customItemReader() {
        return new CustomItemReader();
    }

    @Bean
    public JdbcBatchItemWriter<Line> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Line>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO line (text) VALUES (:text)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public LineItemProcessor processor() {
        return new LineItemProcessor();
    }

    @Bean
    public Step step(JobRepository jobRepository, TransactionManager transactionManager, JdbcBatchItemWriter<Line> writer, CustomItemReader customItemReader) {
        return new StepBuilder("step")
                .repository(jobRepository)
                .transactionManager((PlatformTransactionManager) transactionManager)
                .<Line, Line>chunk(1)
                .reader(customItemReader)
                .processor(processor())
                .writer(writer)
                .build();
    }
}
