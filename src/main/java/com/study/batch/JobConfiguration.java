package com.study.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.*;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
//@Configuration
public class JobConfiguration {

    @Bean
    public Job job(JobRepository jobRepository, Step step) {
        return new JobBuilder("job-chunk", jobRepository)
                .start(step)
                .build();
    }

//    @Bean
//    @JobScope
//    public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
//
//        return new StepBuilder("step", jobRepository)
//                .tasklet((a, b) -> {
//                    return RepeatStatus.FINISHED;
//                }, platformTransactionManager)
//                .build();
//    }




    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {

        final ItemReader<Integer> itemReader = new ItemReader() {
            private int count = 0;
            @Override
            public Integer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                count++;

                log.info("Read {}", count);
                if(count == 20)
                    return null;

                if(count >= 15)
                    throw new IllegalStateException("예외가 발생했어요");


                return count;
            }
        };

        final ItemProcessor<Integer, Integer> itemProcessor = new ItemProcessor<Integer, Integer>() {
            @Override
            public Integer process(Integer item) throws Exception {

                if(item == 15)
                    throw new IllegalStateException();

                return item;
            }
        };


        return new StepBuilder("step", jobRepository)
                .<Integer, Integer>chunk(10, platformTransactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(read -> {})
                .faultTolerant()
                .retry(IllegalStateException.class)
                .retryLimit(5)
                .build();
    }
}
