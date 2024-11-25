package com.iot.airsense.repository;

import com.iot.airsense.model.AqiSummary;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AqiSummaryRepository extends MongoRepository<AqiSummary, String> {
}
