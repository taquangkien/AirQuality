package com.iot.airsense.repository;

import com.iot.airsense.model.AqiSummary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AqiSummaryRepository extends MongoRepository<AqiSummary, String> {
    Optional<AqiSummary> findByLocationAndStartTime(String location, LocalDateTime startTime);
}
