package com.github.gpor0.jooreo;

import java.util.List;
import java.util.stream.Stream;

public class Queried<T> {

    private final Long totalCount;
    private final Stream<T> result;

    private Queried(Long totalCount, Stream<T> result) {
        this.totalCount = totalCount;
        this.result = result;
    }

    public static <T> Queried<T> result(Long totalCount, Stream<T> result) {

        return new Queried<>(totalCount, result);
    }

    public static <T> Queried<T> result(Long totalCount, List<T> result) {

        return new Queried(totalCount, result.stream());
    }

    public static <T> Queried<T> result(Long totalCount, T... result) {

        return new Queried<>(totalCount, Stream.of(result));
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public Stream<T> stream() {
        return result;
    }
}
