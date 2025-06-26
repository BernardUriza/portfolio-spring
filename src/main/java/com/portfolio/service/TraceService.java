package com.portfolio.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TraceService {
    public void record(String info) {
        log.info("User trace: {}", info);
    }
}
