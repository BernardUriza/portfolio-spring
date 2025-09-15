package com.portfolio.service;

import com.portfolio.adapter.out.persistence.jpa.SyncConfigJpaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class SyncConfigServiceConcurrencyTest {

    @Autowired
    private SyncConfigService service;

    @Autowired
    private SyncConfigJpaRepository repo;

    @Test
    void concurrentGetOrCreate_ShouldLeaveSingleRow() throws Exception {
        int threads = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        List<Throwable> errors = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await(5, TimeUnit.SECONDS);
                    service.getOrCreate();
                } catch (Throwable t) {
                    synchronized (errors) { errors.add(t); }
                } finally {
                    done.countDown();
                }
            });
        }

        // fire!
        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        done.await(10, TimeUnit.SECONDS);
        pool.shutdownNow();

        if (!errors.isEmpty()) {
            errors.forEach(Throwable::printStackTrace);
        }
        Assertions.assertTrue(errors.isEmpty(), "No errors expected during concurrent getOrCreate");
        Assertions.assertEquals(1L, repo.count(), "sync_config must have exactly one row");
    }
}

