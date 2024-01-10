package com.study.batch.application;

import com.study.batch.EmailProvider;
import com.study.batch.batch.JobExecution;
import com.study.batch.batch.JobExecutionListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DormantBatchJobExecutionListener implements JobExecutionListener {

    private final EmailProvider emailProvider;

    public DormantBatchJobExecutionListener() {
        this.emailProvider = new EmailProvider.Fake();
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        emailProvider.send("admin@test.com", "배치 완료 알림","DormantBatchJob이 수행되었습니다. status "+jobExecution.getStatus());
    }
}
