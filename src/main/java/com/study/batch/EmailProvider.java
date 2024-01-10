package com.study.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

public interface EmailProvider {

    void send(String emailAddress, String title, String body);

    @Slf4j
    class Fake implements EmailProvider{

        @Override
        public void send(String emailAddress, String title, String body) {
            log.info("{} email 전송 완료! {} : {}", emailAddress, title, body);
        }
    }
}
