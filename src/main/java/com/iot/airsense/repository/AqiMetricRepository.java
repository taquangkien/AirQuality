package com.iot.airsense.repository;

import com.iot.airsense.model.AqiMetric;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AqiMetricRepository extends MongoRepository<AqiMetric, String> {
}
