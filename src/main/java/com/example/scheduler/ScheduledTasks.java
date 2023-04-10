package com.example.scheduler;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Supplier;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@EnableAsync
public class ScheduledTasks {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    RateLimiterConfig config = RateLimiterConfig.custom()
            .limitForPeriod(1)
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofSeconds(5))
            .build();

    RateLimiterRegistry registry = RateLimiterRegistry.of(config);
    RateLimiter limiter = registry.rateLimiter("GetGreetings");

    Supplier<Void> GreetingSupplier =
            RateLimiter.decorateSupplier(limiter,
                    () -> {
                        String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
                        log.info("Thread " + Thread.currentThread().getId() + ", Time: " + timeStamp + ", greeting: " + callAndGetGreeting());
                        return null;
                    });

    @Async
    @Scheduled(fixedRate = 200)
    public void Greet() {
        log.info("      Thread " + Thread.currentThread().getId());
        GreetingSupplier.get();
    }

    private String callAndGetGreeting() {
        String uri = "http://localhost:8080/greeting?name=Kishore";
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(uri, String.class);
    }
}
