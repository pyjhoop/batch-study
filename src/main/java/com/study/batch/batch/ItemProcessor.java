package com.study.batch.batch;

public interface ItemProcessor<I, O> {

    O process(I item);
}
