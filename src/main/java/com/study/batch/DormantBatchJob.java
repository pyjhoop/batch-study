package com.study.batch;

import com.study.batch.batch.BatchStatus;
import com.study.batch.batch.JobExecution;
import com.study.batch.customer.Customer;
import com.study.batch.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DormantBatchJob {

    private final CustomerRepository customerRepository;
    private final EmailProvider emailProvider;

    public DormantBatchJob(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
        this.emailProvider = new EmailProvider.Fake();
    }

    public JobExecution execute() {

        final JobExecution jobExecution = new JobExecution();
        jobExecution.setStatus(BatchStatus.STARTING);
        jobExecution.setStartTime(LocalDateTime.now());

        int pageNo = 0;

        try{
            while (true) {
                // 1. 유저를 조회한다. -> 휴먼 대상 고객만 가져올수 있고 하나씩 다 가져올 수 있다.
                final PageRequest pageRequest = PageRequest.of(pageNo, 1, Sort.by("id").ascending());
                final Page<Customer> page = customerRepository.findAll(pageRequest);

                final Customer customer;
                if(page.isEmpty()){
                    break;
                } else {
                    pageNo++;
                    customer = page.getContent().get(0);
                }

                // 2. 휴면계정 대상을 추출 및 반환한다.
                final boolean isDormantTarget = LocalDate.now()
                        .minusDays(365)
                        .isAfter(customer.getLoginAt().toLocalDate());

                if(isDormantTarget) {
                    customer.setStatus(Customer.Status.DORMANT);
                } else {
                    continue;
                }

                // 3. 휴면계정으로 상태를 변경한다.
                customerRepository.save(customer);
                // 4. 휴면계정 전환했다고 메일을 보낸다.
                emailProvider.send(customer.getEmail(), "휴먼전환 안내 메일입니다.", "내용");

            }
            jobExecution.setStatus(BatchStatus.COMPLETED);

        } catch (Exception e){
            jobExecution.setStatus(BatchStatus.FAILED);
        }

        jobExecution.setEndTime(LocalDateTime.now());

        emailProvider.send("admin@test.com", "배치 완료 알림","DormantBatchJob이 수행되었습니다. status "+jobExecution.getStatus());

        return jobExecution;
    }
}
