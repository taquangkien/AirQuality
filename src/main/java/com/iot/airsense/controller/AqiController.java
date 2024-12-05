package com.iot.airsense.controller;
;
import com.iot.airsense.model.*;
import com.iot.airsense.repository.AirQualityRepository;
import com.iot.airsense.repository.AqiSummaryRepository;
import com.iot.airsense.repository.AverageAirQualityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.iot.airsense.util.AqiCalculator.calculateHourlyAqiMetric;
import static com.iot.airsense.util.AqiCalculator.calculateNowcast;


@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class AqiController {
    private final AirQualityRepository airQualityRepository;
    private final AverageAirQualityRepository averageAirQualityRepository;
    private final AqiSummaryRepository aqiSummaryRepository;

    @GetMapping()
    public void calculateHourlyAqi() {
        LocalDateTime now = LocalDateTime.now().minusHours(5).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime oneHourAgo = now.minusHours(1);

        List<String> locations = airQualityRepository.findDistinctLocations().stream().filter(Objects::nonNull).toList();

        for (String location : locations) {
            List<AirQuality> hourlyData = airQualityRepository.findByLocationAndTimestampBetween(location, oneHourAgo, now.minusSeconds(1));
            if (!hourlyData.isEmpty()) {
                // Tính giá trị Nowcast cho PM2.5
                double currentHourPm25Avg = hourlyData.stream().mapToDouble(AirQuality::getPm25).average().orElse(0.0);
                double currentHourCoAvg = hourlyData.stream().mapToDouble(AirQuality::getCo).average().orElse(0.0);
                AverageAirQuality averageAirQuality = AverageAirQuality.builder()
                        .location(location)
                        .averagePm25(currentHourPm25Avg)
                        .averageCo(currentHourCoAvg)
                        .startTime(oneHourAgo)
                        .build();
//                averageAirQualityRepository.save(averageAirQuality);
                double pm25Nowcast = calculateNowcast(getLatest12AveragePm25(location));
                int pm25Aqi = calculateHourlyAqiMetric(pm25Nowcast, MetricType.PM25);

                int coAqi = calculateHourlyAqiMetric(currentHourCoAvg, MetricType.CO);

                int compositeAqi = Math.max(pm25Aqi, coAqi);

                AqiSummary hourlyAqi = AqiSummary.builder()
                        .aqi(compositeAqi)
                        .period(PeriodType.HOUR)
                        .startTime(oneHourAgo)
                        .location(location)
                        .build();
//                aqiSummaryRepository.save(hourlyAqi);
            }
        }
    }

    public List<Double> getLatest12AveragePm25(String location) {
        Pageable pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "startTime"));
        return averageAirQualityRepository.findTop12AveragePm25ByLocation(location, pageable);
    }
}
