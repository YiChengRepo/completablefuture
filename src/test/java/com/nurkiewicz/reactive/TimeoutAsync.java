package com.nurkiewicz.reactive;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimeoutAsync {

    public static final Logger log = LoggerFactory.getLogger(TimeoutAsync.class);

    private static final ScheduledExecutorService pool =
            Executors.newScheduledThreadPool(10,
                    new ThreadFactoryBuilder()
                            .setDaemon(true)
                            .setNameFormat("FutureOps-%d")
                            .build()
            );

    public static <T> CompletableFuture<T> timeoutAfter(Duration duration) {
        final CompletableFuture<T> promise = new CompletableFuture<>();
        pool.schedule(
                () -> promise.completeExceptionally(new TimeoutException()),
                duration.toMillis(), TimeUnit.MILLISECONDS);
        return promise;
    }

    private static String process(long processTime) {
        System.out.println("calling process 1 " + Thread.currentThread());
        pause(processTime);
        return "process 1";
    }

    private static String process2(long processTime) {
        System.out.println("calling process 2 " + Thread.currentThread());
        pause(processTime);
        return "process 2";
    }

    private static void pause(long millisecond) {
        try {
            Thread.sleep(millisecond);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void asyncFutureTimeout(int timeoutSecond, long processMilliSecond) {
        CompletableFuture<String> timeoutAfter = timeoutAfter(Duration.ofSeconds(timeoutSecond));
        CompletableFuture<String> future = CompletableFuture.supplyAsync( () -> process(processMilliSecond));
        future.applyToEither(timeoutAfter, str -> {
            System.out.println("result is : " + str);
            return str;
        }).exceptionally(e -> {
            System.out.println("exception caught" + e.getCause());
            return null;
        });

        System.out.println("end");
        pause(5000);

    }

    private void asyncFutureTimeout(int timeoutSecond, long processMillisecond1, long processMillisecond2) {
        final CompletableFuture<String> java = CompletableFuture.supplyAsync(() -> process(processMillisecond1));
        final CompletableFuture<String> java2 = CompletableFuture.supplyAsync(() -> process2(processMillisecond2));
        final CompletableFuture<String> timeout = S09_Promises.timeoutAfter(Duration.ofSeconds(timeoutSecond));

        final CompletableFuture<Object> firstCompleted =
                CompletableFuture.anyOf(java,timeout, java2);

        firstCompleted.thenAccept((Object result) -> {
            log.debug("First: {}", result);
        }).exceptionally(e -> {
            System.out.println("exception caught" + e.getCause());
            return null;
        });

        log.debug("ending");
        pause(5000);
        log.debug("end done");
    }

    @Test
    public void task1Finish1st() throws Exception {
        asyncFutureTimeout(4, 1000,2000);
    }


    @Test
    public void task2Finish1st() throws Exception {
        asyncFutureTimeout(4, 3000,2000);
    }


    @Test
    public void timeoutFinish1st() throws Exception {
        asyncFutureTimeout(2, 4000,3000);
    }


    @Test
    public void processFinishBeforeTimeout() {
        asyncFutureTimeout(5, 2000);

    }

    @Test
    public void testTimeoutBeforeTaskFinish() {
        asyncFutureTimeout(1, 2000);
    }


}
