package com.iot.airsense.repository;

import com.iot.airsense.model.AirQuality;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AirQualityRepository extends MongoRepository<AirQuality, String> {
    List<AirQuality> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<AirQuality> findByLocationAndTimestampBetween(String location, LocalDateTime start, LocalDateTime end);

    @Query(value = "{}", fields = "{'location': 1}")
    List<String> findDistinctLocations();
}
