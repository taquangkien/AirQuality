package com.iot.airsense.repository;

import com.iot.airsense.model.SampleData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SampleRepository extends MongoRepository<SampleData, String> {
}
