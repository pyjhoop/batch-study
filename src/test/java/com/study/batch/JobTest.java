package com.study.batch;

import com.study.batch.batch.BatchStatus;
import com.study.batch.batch.Job;
import com.study.batch.batch.JobExecution;
import com.study.batch.customer.Customer;
import com.study.batch.customer.CustomerRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;


@SpringBootTest
class JobTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private Job job;

    @BeforeEach
    public void setup(){
        customerRepository.deleteAll();
    }

    @Test
    @DisplayName("로그인 시간이 일년을 경과한 고객이 세명이고, 일년 이내에 로그인한 고객이 다섯명이면 3명의 고객이 휴먼전환 대상이다.")
    void test1() {

        //given
        saveCustomer(366);
        saveCustomer(366);
        saveCustomer(366);
        saveCustomer(364);
        saveCustomer(364);
        saveCustomer(364);
        saveCustomer(364);
        saveCustomer(364);

        // when
        final JobExecution result = job.execute();

        // then
        final long dormantCount = customerRepository.findAll()
                .stream()
                .filter(it -> it.getStatus() == Customer.Status.DORMANT)
                .count();

        Assertions.assertThat(dormantCount).isEqualTo(3);
        Assertions.assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }


    @Test
    @DisplayName("고객이 10명 있지만 모두 다 휴먼전환 대상이면 휴먼 전환 대상은 10명이다.")
    void test2() {
        // given
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);


        // when
        final JobExecution result = job.execute();

        // then
        final long dormantCount = customerRepository.findAll()
                .stream()
                .filter(it -> it.getStatus() == Customer.Status.DORMANT)
                .count();

        Assertions.assertThat(dormantCount).isEqualTo(10);
        Assertions.assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    @DisplayName("고객이 없는 경우에도 배치는 정상동작해야한다.") // 예상치 못한 동작에서 버그가 발행할 경우가 존재하기 때문이다.
    void test3() {

        // when
        final JobExecution result = job.execute();

        // then
        final long dormantCount = customerRepository.findAll()
                .stream()
                .filter(it -> it.getStatus() == Customer.Status.DORMANT)
                .count();

        Assertions.assertThat(dormantCount).isEqualTo(0);
        Assertions.assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    }

    @Test
    @DisplayName("배치가 실패하면 BatchStatus는 FAILED를 반환해야한다.")
    void test4(){
        // given
        final Job job = new Job(null);

        // when
        final JobExecution result = job.execute();

        // then
        Assertions.assertThat(result.getStatus()).isEqualTo(BatchStatus.FAILED);
    }


    private void saveCustomer(long loginMinusDays) {
        final String uuid = UUID.randomUUID().toString();
        final Customer test = new Customer(uuid, uuid+"@tset.com");
        test.setLoginAt(LocalDateTime.now().minusDays(loginMinusDays));
        customerRepository.save(test);
    }
}