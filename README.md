# Introduction

This is a demo project build upon the **Spring Batch** framework to give
a shell solution for the Taxonomic Job Management.  
Versions are compatible with Dspace project in case there is a need to
include it as a module, but it can be used as a separate service as
well.

## Project

The project is split between three main packages,

1.  common

2.  core

3.  rest

The **common** package includes custom exceptions and utils used
throughout the project.  
The **core** package contains the batch configs and components, domain
and services components.  
The **rest** package is self explanatory, it contains rest controllers
and dto’s.

Versions used are:

    <java.version>11</java.version>
    <spring.version>5.3.20</spring.version>

as well as the spring boot parent:

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.6.8</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

Versions are compatible with DSpace project in case there is a need to
include it as a module.

## Build and test

Open a terminal and navigate to the project root:  
*mvn clean install* (Optionally, if you want to skip running tests use
mvn clean install -Dmaven.test.skip=true)

## Run application

For the moment you can start the application using only maven:  
*mvn spring-boot:run*

# Batch

## Configuration

Configs under *core.batch.config* package.

### Job management Interfaces

Can be found in the class *BatchConfiguration*. Here we can find the
different implementations given to the core interfaces of spring
batch.  
Further explanation:

1.  DataSource batchDataSource → configured hsql datasource

2.  JdbcTemplate jdbcTemplateDatasource → jdbc template created from
    configured datasource

3.  JdbcOperations jdbcTemplate → one of spring batch core interfaces,
    used for database interaction and injected in the default repository
    implementations of other core interfaces

4.  JobExecutionDao jobExecutionDao → repository for job executions

5.  JobInstanceDao jobInstanceDao → repository for job instances

6.  JobLauncher jobLauncher → interface for launching jobs (implemented
    by CustomJobLauncher for maintaining a one-to-one relationship
    between jobs and job executions)

7.  JobRegistry jobRegistry → repository for jobs, for the moment uses a
    default implementation (which uses a map to store the jobs) but must
    be changed to an implementation that stores jobs in the database for
    persistence (not application lifecycle)

8.  JobOperator jobOperator → used for starting, stopping, restarting
    jobs

*Note:*  
Annotation @EnableBatchProcessing provides core beans implementations
(check out comment above the job operator bean).  
Important to understand that spring batch uses these interfaces in the
background (called within other core interfaces, like job operator uses
the job launcher) but you can also choose to inject them in your
services.

### Static Job’s

Check out the class *JobConfiguration*. This configuration class is only
for demonstrative purposes, it contains the required beans to configure
and create a job.

## Components

Batch components are separated in the following pattern:

1.  readers

2.  processors

3.  writters

4.  listeners

Names are self explanatory, but the key thing is how to restart the job
at the specific point where it stopped reading. Thats why the readers
must maintain a state (for the last read) and expose methods to start or
stop the read based on the job status. *Check out the
JobCompletionNotificationListener and CustomItemReader components.*

# Custom components

## Job execution

This service is used for CRUD operations on jobs and job executions.
Same endpoint is used for updating or saving a job, based on the
parameter jobExecutionId if its null or not.  
If the jobExecutionId is null we create a job in the job registry, else
we update the job with the new values and return the already existing
job execution.  
This is done like this because the job executions is a running instance
of a job, so if we update only the job values (readers, processors,
writters) there is not need to update the job execution, since it
already has a reference to the job and will use the new components with
the new values when started again.

## Custom Repositories

A CustomRepositoryFactory is used to return the wanted instance of the
custom repositories, these repositories extend the
AbstractJdbcBatchMetadataDao which is a spring batch abstract class.  
These repositories are used to extend the default capabilities of spring
batch.

# Tests

No tests are done until now. Will wait for the development period.
