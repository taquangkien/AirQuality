package com.iot.airsense.repository;

import com.iot.airsense.model.AqiSummary;
import com.iot.airsense.model.PeriodType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AqiSummaryRepository extends MongoRepository<AqiSummary, String> {
    Optional<AqiSummary> findByLocationAndStartTime(String location, LocalDateTime startTime);

    @Query("{ 'location': ?0, 'period': ?1, 'startTime': { $gte: ?2, $lte: ?3 } }")
    List<AqiSummary> findByLocationAndPeriodAndStartTimeBetween(String location, PeriodType period,
                                                                LocalDateTime start, LocalDateTime end);
}
