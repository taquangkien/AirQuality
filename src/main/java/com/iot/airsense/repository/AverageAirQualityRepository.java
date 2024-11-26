package com.iot.airsense.repository;

import com.iot.airsense.model.AverageAirQuality;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AverageAirQualityRepository extends MongoRepository<AverageAirQuality, String> {
    // Lấy 12 object mới nhất theo location, sắp xếp giảm dần theo startTime
    List<AverageAirQuality> findTop12ByLocationOrderByStartTimeDesc(String location);

    List<AverageAirQuality> findByLocationAndStartTimeBetween(String location, LocalDateTime start, LocalDateTime end);

    // Lấy trực tiếp danh sách các giá trị averagePm25
    @Query(value = "{ 'location': ?0 }", fields = "{ 'averagePm25': 1 }")
    List<Double> findTop12AveragePm25ByLocation(String location, Pageable pageable);
}
