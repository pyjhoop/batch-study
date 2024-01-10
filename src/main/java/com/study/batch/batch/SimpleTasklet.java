package com.study.batch.batch;

import com.study.batch.batch.ItemProcessor;
import com.study.batch.batch.ItemReader;
import com.study.batch.batch.ItemWriter;
import com.study.batch.batch.Tasklet;
import com.study.batch.customer.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SimpleTasklet<I, O> implements Tasklet {

    private final ItemReader<I> itemReader;
    private final ItemProcessor<I, O> itemProcessor;
    private final ItemWriter<O> itemWriter;

    public SimpleTasklet(ItemReader<I> itemReader, ItemProcessor<I, O> itemProcessor, ItemWriter<O> itemWriter) {
        this.itemReader = itemReader;
        this.itemProcessor = itemProcessor;
        this.itemWriter = itemWriter;
    }

    @Override
    public void execute() {
//         비즈니스 로직
        int pageNo = 0;
        while (true) {

            // read
            final I read = itemReader.read();
            if(read == null) break;
            // process
            final O process = itemProcessor.process(read);
            if(process == null) continue;
            // write
            itemWriter.write(process);

        }
    };
}
