package com.example.core.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.metrics.BatchMetrics;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Iterator;

public class CustomJobLauncher implements JobLauncher, InitializingBean {
    protected static final Log logger = LogFactory.getLog(CustomJobLauncher.class);
    private JobRepository jobRepository;
    private TaskExecutor taskExecutor;

    public CustomJobLauncher() {
    }

    public JobExecution run(final Job job, final JobParameters jobParameters) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        Assert.notNull(job, "The Job must not be null.");
        Assert.notNull(jobParameters, "The JobParameters must not be null.");
        JobExecution lastExecution = this.jobRepository.getLastJobExecution(job.getName(), jobParameters);
        if (lastExecution != null) {
            if (!job.isRestartable()) {
                throw new JobRestartException("JobInstance already exists and is not restartable");
            }

            Iterator var5 = lastExecution.getStepExecutions().iterator();

            while (var5.hasNext()) {
                StepExecution execution = (StepExecution) var5.next();
                BatchStatus status = execution.getStatus();
                if (status.isRunning() || status == BatchStatus.STOPPING) {
                    throw new JobExecutionAlreadyRunningException("A job execution for this job is already running: " + lastExecution);
                }

                if (status == BatchStatus.UNKNOWN) {
                    throw new JobRestartException("Cannot restart step [" + execution.getStepName() + "] from UNKNOWN status. The last execution ended with a failure that could not be rolled back, so it may be dangerous to proceed. Manual intervention is probably necessary.");
                }
            }
        }

        job.getJobParametersValidator().validate(jobParameters);

        /** removed the creation of another job execution: this.jobRepository.createJobExecution(job.getName(), jobParameters);*/
        final JobExecution jobExecution = lastExecution;

        try {
            this.taskExecutor.execute(new Runnable() {
                public void run() {
                    try {
                        if (CustomJobLauncher.logger.isInfoEnabled()) {
                            CustomJobLauncher.logger.info("Job: [" + job + "] launched with the following parameters: [" + jobParameters + "]");
                        }

                        job.execute(jobExecution);
                        if (CustomJobLauncher.logger.isInfoEnabled()) {
                            Duration jobExecutionDuration = BatchMetrics.calculateDuration(jobExecution.getStartTime(), jobExecution.getEndTime());
                            CustomJobLauncher.logger.info("Job: [" + job + "] completed with the following parameters: [" + jobParameters + "] and the following status: [" + jobExecution.getStatus() + "]" + (jobExecutionDuration == null ? "" : " in " + BatchMetrics.formatDuration(jobExecutionDuration)));
                        }
                    } catch (Throwable var2) {
                        if (CustomJobLauncher.logger.isInfoEnabled()) {
                            CustomJobLauncher.logger.info("Job: [" + job + "] failed unexpectedly and fatally with the following parameters: [" + jobParameters + "]", var2);
                        }

                        this.rethrow(var2);
                    }

                }

                private void rethrow(Throwable t) {
                    if (t instanceof RuntimeException) {
                        throw (RuntimeException) t;
                    } else if (t instanceof Error) {
                        throw (Error) t;
                    } else {
                        throw new IllegalStateException(t);
                    }
                }
            });
        } catch (TaskRejectedException var8) {
            jobExecution.upgradeStatus(BatchStatus.FAILED);
            if (jobExecution.getExitStatus().equals(ExitStatus.UNKNOWN)) {
                jobExecution.setExitStatus(ExitStatus.FAILED.addExitDescription(var8));
            }

            this.jobRepository.update(jobExecution);
        }

        return jobExecution;
    }

    public void setJobRepository(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(this.jobRepository != null, "A JobRepository has not been set.");
        if (this.taskExecutor == null) {
            logger.info("No TaskExecutor has been set, defaulting to synchronous executor.");
            this.taskExecutor = new SyncTaskExecutor();
        }

    }
}
