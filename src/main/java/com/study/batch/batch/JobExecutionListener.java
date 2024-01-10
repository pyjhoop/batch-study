package com.study.batch.batch;

public interface JobExecutionListener {

    void beforeJob(JobExecution jobExecution);
    void afterJob(JobExecution jobExecution);

}
