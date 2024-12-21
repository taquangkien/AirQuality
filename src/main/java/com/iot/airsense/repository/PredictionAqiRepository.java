package com.iot.airsense.repository;

import com.iot.airsense.model.PredictionAqi;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PredictionAqiRepository extends MongoRepository<PredictionAqi, String> {
    @Query("{ 'startTime' : { $gte: ?0 } }")
    List<PredictionAqi> findByStartTimeFrom(LocalDateTime startTime);
}
