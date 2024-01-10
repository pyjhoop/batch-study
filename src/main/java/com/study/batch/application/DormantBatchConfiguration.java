package com.study.batch.application;

import com.study.batch.batch.Job;
import com.study.batch.batch.SimpleTasklet;
import com.study.batch.customer.Customer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DormantBatchConfiguration {

    @Bean
    public Job dormantBatchJob(
            DormantBatchItemReader itemReader,
            DormantBatchItemProcessor itemProcessor,
            DormantBatchItemWriter itemWriter,
            DormantBatchJobExecutionListener dormantBatchJobExecutionListener
    ) {
        return Job.builder()
                .itemReader(itemReader)
                .itemProcessor(itemProcessor)
                .itemWriter(itemWriter)
                .jobExecutionListener(dormantBatchJobExecutionListener)
                .build();
    }

}
