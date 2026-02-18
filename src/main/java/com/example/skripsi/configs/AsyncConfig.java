package com.example.skripsi.configs; // (Sesuaikan package Anda)

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.AsyncTaskExecutor; // <-- Penting
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    // 1. BEAN MENTAH
    // Ini adalah thread pool aslinya.
    @Bean
    public ThreadPoolTaskExecutor rawTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(55);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "taskExecutor")
    @Primary
    public AsyncTaskExecutor securityAwareTaskExecutor(ThreadPoolTaskExecutor rawTaskExecutor) {
        //SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);

        return new DelegatingSecurityContextAsyncTaskExecutor(rawTaskExecutor);
    }
}