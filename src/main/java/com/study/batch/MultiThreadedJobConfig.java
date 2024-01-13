package com.study.batch;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class MultiThreadedJobConfig {

    @Bean
    public Job job(
            JobRepository jobRepository,
            Step step
    ){
        return new JobBuilder("multiThreadedJob", jobRepository)
                .start(step)
                .incrementer(new RunIdIncrementer())
                .build();

    }

    @Bean
    public Step step(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JpaPagingItemReader<User> jpaPagingItemReader
    ){
        return new StepBuilder("step", jobRepository)
                .<User, User>chunk(5, transactionManager)
                .reader(jpaPagingItemReader)
                .writer(result -> log.info(result.toString()))
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public JpaPagingItemReader<User> jpaPagingItemReader(
            EntityManagerFactory entityManagerFactory
    ) {
        return new JpaPagingItemReaderBuilder<User>()
                .name("jpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(5)
                .saveState(false) // 1~10, 11~20 11~20에서 예외가 발생해도 멀티스레드이기에 1~10도 제대로 동작했다는 것을 알 수 없기에 재시작 방지
                .queryString("SELECT u FROM User u ORDER BY u.id")
                .build();
    }
}
