package com.portfolio.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {
    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);
    
    @Value("${portfolio.cache.completion.max-size:1000}")
    private int completionCacheMaxSize;
    
    @Value("${portfolio.cache.completion.expire-after-write:PT30M}")
    private Duration completionCacheExpireAfterWrite;
    
    @Value("${portfolio.cache.projects.max-size:500}")
    private int projectsCacheMaxSize;
    
    @Value("${portfolio.cache.projects.expire-after-write:PT15M}")
    private Duration projectsCacheExpireAfterWrite;
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Portfolio completion cache - longer TTL as calculation is expensive
        cacheManager.registerCustomCache("portfolio-completion", 
            Caffeine.newBuilder()
                .maximumSize(completionCacheMaxSize)
                .expireAfterWrite(completionCacheExpireAfterWrite)
                .recordStats()
                .build());
        
        // Portfolio projects cache - shorter TTL for more real-time updates
        cacheManager.registerCustomCache("portfolio-projects", 
            Caffeine.newBuilder()
                .maximumSize(projectsCacheMaxSize)
                .expireAfterWrite(projectsCacheExpireAfterWrite)
                .recordStats()
                .build());
        
        // Portfolio overview cache - medium TTL for aggregate data
        cacheManager.registerCustomCache("portfolio-overview", 
            Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats()
                .build());
        
        log.info("Configured Caffeine cache manager with completion cache (max={}, expire={}), " +
                "projects cache (max={}, expire={})", 
                completionCacheMaxSize, completionCacheExpireAfterWrite,
                projectsCacheMaxSize, projectsCacheExpireAfterWrite);
        
        return cacheManager;
    }
}
