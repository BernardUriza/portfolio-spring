package com.portfolio.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SyncConfigAdminControllerConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void concurrentStatusRequests_ShouldAllReturn200() throws Exception {
        int threads = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        List<Exception> errors = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await(5, TimeUnit.SECONDS);
                    MvcResult res = mockMvc.perform(get("/api/admin/sync-config/status"))
                            .andExpect(status().isOk())
                            .andReturn();
                    String body = res.getResponse().getContentAsString();
                    Assertions.assertTrue(body.contains("enabled"));
                    Assertions.assertTrue(body.contains("intervalHours"));
                } catch (Exception e) {
                    synchronized (errors) { errors.add(e); }
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        done.await(10, TimeUnit.SECONDS);
        pool.shutdownNow();

        if (!errors.isEmpty()) {
            errors.forEach(Throwable::printStackTrace);
        }
        Assertions.assertTrue(errors.isEmpty(), "All concurrent status requests should succeed");
    }
}

