package com.study.batch.application;

import com.study.batch.EmailProvider;
import com.study.batch.batch.ItemWriter;
import com.study.batch.customer.Customer;
import com.study.batch.customer.CustomerRepository;
import org.springframework.stereotype.Component;

@Component
public class DormantBatchItemWriter implements ItemWriter<Customer> {

    private final CustomerRepository customerRepository;
    private final EmailProvider emailProvider;

    public DormantBatchItemWriter(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
        this.emailProvider = new EmailProvider.Fake();
    }

    @Override
    public void write(Customer item) {
        // 3. 휴면계정으로 상태를 변경한다.
        customerRepository.save(item);
        // 4. 휴면계정 전환했다고 메일을 보낸다.
        emailProvider.send(item.getEmail(), "휴먼전환 안내 메일입니다.", "내용");
    }
}
