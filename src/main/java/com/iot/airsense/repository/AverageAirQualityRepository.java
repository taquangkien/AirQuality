package com.iot.airsense.repository;

import com.iot.airsense.model.AverageAirQuality;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AverageAirQualityRepository extends MongoRepository<AverageAirQuality, String> {
    // Lấy 12 object mới nhất theo location, sắp xếp giảm dần theo startTime
    List<AverageAirQuality> findTop12ByLocationOrderByStartTimeDesc(String location);

    List<AverageAirQuality> findByLocationAndStartTimeBetween(String location, LocalDateTime start, LocalDateTime end);

    @Query("{ 'startTime': { $gte: ?0 }, 'location': ?1 }")
    List<AverageAirQuality> findRecentAirQualityByLocation(LocalDateTime fromTime, String location, Sort sort);
}
