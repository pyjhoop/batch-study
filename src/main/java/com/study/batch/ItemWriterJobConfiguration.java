package com.study.batch;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class ItemWriterJobConfiguration {

    @Bean
    public Job job(
            JobRepository jobRepository,
            Step step
    ){
        return new JobBuilder("itemReaderJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

    @Bean
    public Step step(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
            ItemReader<User> flatFileItemReader,
            ItemWriter<User> jdbcBatchItemWriter
    ){
        return new StepBuilder("step", jobRepository)
                .<User, User>chunk(2, platformTransactionManager)
                .reader(flatFileItemReader)
                .writer(jdbcBatchItemWriter)
                .build();
    }

    @Bean
    public FlatFileItemReader<User> flatFileItemReader() {
        return new FlatFileItemReaderBuilder<User>()
                .name("flatFileItemReader")
                .resource(new ClassPathResource("users.txt"))
                .linesToSkip(2)
                .delimited().delimiter(",")// 구분자를 변경할 수 있다.
                .names("name","age","regions", "telephone")
                .targetType(User.class)
                .strict(true) // 기본 true고 파일이 없으면 에러가 발생한다. false면 에러가 발생하지 않고 그냥 넘어간다.
                .build();
    }

    @Bean
    public ItemWriter<User> flatFileItemWriter() {
        return new FlatFileItemWriterBuilder<User>()
                .name("flatFileItemWriter")
                .resource(new PathResource("src/main/resources/new_users.txt"))
                .delimited().delimiter("__")
                .names("name","age","region","telephone")
                .build();
    }

    @Bean
    public ItemWriter<User> formattedFlatFileItemWriter() {
        return new FlatFileItemWriterBuilder<User>()
                .name("flatFileItemWriter")
                .resource(new PathResource("src/main/resources/new_formatted_users.txt"))
                .formatted()
                .format("%s의 나이는 %s입니다. 사는곳은 %s, 전화번호는 %s입니다.")
                .names("name","age","region","telephone")
                .shouldDeleteIfExists(false) // 파일이 존재하면 삭제하고 다시 만들어라. false인경우 오류발생한다.
                .append(true) // 파일이 이미존재하면 데이터를 추가해라라는 의미이다.
                .build();
    }

    @Bean
    public JsonFileItemWriter<User> jsonFileItemWriter() {
        return new JsonFileItemWriterBuilder<User>()
                .name("jsonFileItemWriter")
                .resource(new PathResource("src/main/resources/new_users.json"))
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .build();
    }


    // 아이템 하나씩 업데이트 하기에 속도적인 측면에서 않좋을 수 있다.
    @Bean
    public ItemWriter<User> jpaItemWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<User>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    // 청크단위로 벌크로 데이터를 수정해서 작성하기에 데이터가 많으면 jdbc를 사용하는게 좋다.

    @Bean
    public ItemWriter<User> jdbcBatchItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<User>()
                .dataSource(dataSource)
                .sql(
                        """
                                INSERT INTO 
                                    USER(name, age, region, telephone)
                                VALUES
                                    (:name, :age, :region, :telephone)
                                
                                """
                )
                .beanMapped()
                .build();
    }
}
