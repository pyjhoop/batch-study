package com.study.batch.batch;

import com.study.batch.batch.BatchStatus;
import com.study.batch.batch.JobExecution;
import com.study.batch.batch.JobExecutionListener;
import com.study.batch.batch.Tasklet;
import com.study.batch.customer.Customer;
import com.study.batch.customer.CustomerRepository;
import lombok.Builder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;


public class Job {
    // 비즈니스 로직의 하나의 단위를 tasklet
    // 전처리기 후처리기
    private final Tasklet tasklet;
    private final JobExecutionListener jobExecutionListener;

    public Job(Tasklet tasklet) {
        this(tasklet, null);
    }

    @Builder
    public Job(ItemReader<?> itemReader, ItemProcessor<?,?> itemProcessor, ItemWriter<?> itemWriter, JobExecutionListener jobExecutionListener) {
        this(new SimpleTasklet(itemReader, itemProcessor, itemWriter), jobExecutionListener);
    }

    public Job(Tasklet tasklet, JobExecutionListener jobExecutionListener) {
        this.tasklet = tasklet;
        this.jobExecutionListener = Objects.requireNonNullElseGet(jobExecutionListener, () -> new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {

            }

            @Override
            public void afterJob(JobExecution jobExecution) {

            }
        });
    }

    public JobExecution execute() {

        final JobExecution jobExecution = new JobExecution();
        jobExecution.setStatus(BatchStatus.STARTING);
        jobExecution.setStartTime(LocalDateTime.now());
        //전처리
        jobExecutionListener.beforeJob(jobExecution);


        try{
            // 비즈니스 로직 처리
            tasklet.execute();
            jobExecution.setStatus(BatchStatus.COMPLETED);

        } catch (Exception e){
            jobExecution.setStatus(BatchStatus.FAILED);
        }

        jobExecution.setEndTime(LocalDateTime.now());

        //후처리
        jobExecutionListener.afterJob(jobExecution);

        return jobExecution;
    }
}
