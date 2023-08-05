package com.example.core.domain.repositories;

import com.example.common.exceptions.TypeNotFoundException;
import com.example.core.domain.repositories.impl.JobExecutionCustomRepository;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;

@Component
public class CustomRepositoryFactory {

    public static final String TYPE_OF_CUSTOM_REPOSITORY_NOT_FOUND = "Type of custom repository not found!";
    private final JdbcOperations jdbcTemplate;
    private final JobInstanceDao jobInstanceDao;

    public CustomRepositoryFactory(JdbcOperations jdbcTemplate, JobInstanceDao jobInstanceDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.jobInstanceDao = jobInstanceDao;
    }

    public ICustomRepository getRepository(Type type) throws TypeNotFoundException {

        switch (type) {
            case JOB_EXECUTION:
                return  new JobExecutionCustomRepository(jdbcTemplate, jobInstanceDao);
            default:
                throw new TypeNotFoundException(TYPE_OF_CUSTOM_REPOSITORY_NOT_FOUND);
        }
    }

    public enum Type {
        JOB_EXECUTION
    }
}
