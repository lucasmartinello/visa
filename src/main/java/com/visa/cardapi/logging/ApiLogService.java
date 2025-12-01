package com.visa.cardapi.logging;
import org.springframework.stereotype.Service;

@Service
public class ApiLogService {
    private final ApiLogRepository repo;

    public ApiLogService(ApiLogRepository repo) {
        this.repo = repo;
    }

    public void save(ApiLog log) {
        repo.save(log);
    }
}