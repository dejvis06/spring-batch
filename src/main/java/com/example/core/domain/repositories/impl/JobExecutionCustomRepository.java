package com.example.core.domain.repositories.impl;

import com.example.core.domain.repositories.ICustomRepository;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JobExecutionCustomRepository extends AbstractJdbcBatchMetadataDao implements ICustomRepository<JobExecution, Long> {

    private static final String FIND_JOB_EXECUTIONS_PAGEABLE
            = "SELECT JOB_EXECUTION_ID, START_TIME, END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, CREATE_TIME, LAST_UPDATED, VERSION, JOB_CONFIGURATION_LOCATION, JOB_INSTANCE_ID from %PREFIX%JOB_EXECUTION order by JOB_EXECUTION_ID desc LIMIT ? OFFSET ?";
    private static final String GET_EXECUTION_BY_ID
            = "SELECT JOB_EXECUTION_ID, START_TIME, END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, CREATE_TIME, LAST_UPDATED, VERSION, JOB_CONFIGURATION_LOCATION, JOB_INSTANCE_ID from %PREFIX%JOB_EXECUTION where JOB_EXECUTION_ID = ?";

    private static final String DELETE_JOB_EXECUTION
            = "DELETE FROM %PREFIX%JOB_EXECUTION WHERE JOB_EXECUTION_ID = ?";
    private final JobInstanceDao jobInstanceDao;

    public JobExecutionCustomRepository(JdbcOperations jdbcTemplate, JobInstanceDao jobInstanceDao) {
        this.setJdbcTemplate(jdbcTemplate);
        this.jobInstanceDao = jobInstanceDao;
    }

    @Override
    public List<JobExecution> findAll(Pageable pageable) {
        String limit = String.valueOf(pageable.getPageSize());
        String offset = String.valueOf(pageable.getOffset());

        return this.getJdbcTemplate().query(this.getQuery(FIND_JOB_EXECUTIONS_PAGEABLE), new Object[]{limit, offset}, new JobExecutionRowMapper());
    }

    @Override
    public JobExecution find(Long executionId) {
        try {
            JobExecution jobExecution = (JobExecution) this.getJdbcTemplate().queryForObject(this.getQuery(GET_EXECUTION_BY_ID), new Object[]{executionId}, new JobExecutionRowMapper());
            return jobExecution;
        } catch (EmptyResultDataAccessException var3) {
            return null;
        }
    }

    @Override
    public void delete(Long executionId) {
        this.getJdbcTemplate().update(this.getQuery(DELETE_JOB_EXECUTION), executionId);
    }

    private final class JobExecutionRowMapper implements RowMapper<JobExecution> {

        @Override
        public JobExecution mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong(1);
            JobExecution jobExecution = new JobExecution(id);
            jobExecution.setStartTime(rs.getTimestamp(2));
            jobExecution.setEndTime(rs.getTimestamp(3));
            jobExecution.setStatus(BatchStatus.valueOf(rs.getString(4)));
            jobExecution.setExitStatus(new ExitStatus(rs.getString(5), rs.getString(6)));
            jobExecution.setCreateTime(rs.getTimestamp(7));
            jobExecution.setLastUpdated(rs.getTimestamp(8));
            jobExecution.setVersion(rs.getInt(9));
            jobExecution.setJobInstance(jobInstanceDao.getJobInstance(rs.getLong(10)));

            return jobExecution;
        }
    }
}
