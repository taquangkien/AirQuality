package com.iot.airsense.controller;

import com.iot.airsense.model.*;
import com.iot.airsense.repository.AirQualityRepository;
import com.iot.airsense.repository.AqiSummaryRepository;
import com.iot.airsense.repository.AverageAirQualityRepository;
import com.iot.airsense.service.AqiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.iot.airsense.util.AqiCalculator.calculateHourlyAqiMetric;
import static com.iot.airsense.util.AqiCalculator.calculateNowcast;

@RestController
@Slf4j
@RequestMapping("/api/v1/aqi")
@RequiredArgsConstructor
public class AqiController {
    private final AqiService aqiService;

    @GetMapping("/latest")
    public ResponseEntity<AqiSummary> getLatestHourAqi(@RequestParam String location) {
        return ResponseEntity.ok(aqiService.getLatestHourAqi(location));
    }

    @GetMapping("/summary")
    public ResponseEntity<List<AqiSummary>> getAqiSummary(
            @RequestParam String location,
            @RequestParam PeriodType period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(aqiService.getAqiSummary(location, period, start, end));
    }


    private final AirQualityRepository airQualityRepository;
    private final AverageAirQualityRepository averageAirQualityRepository;
    private final AqiSummaryRepository aqiSummaryRepository;

    @GetMapping("/test")
    public void calculateHourlyAqi() {
        LocalDateTime now = LocalDateTime.now().minusHours(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime oneHourAgo = now.minusHours(1);

        List<String> locations = airQualityRepository.findDistinctLocations()
                .stream().filter(Objects::nonNull).toList();

        for (String location : locations) {
            List<AirQuality> hourlyData = airQualityRepository
                    .findByLocationAndTimestampBetween(location, oneHourAgo, now.minusSeconds(1));
            if (!hourlyData.isEmpty()) {
                // Tính giá trị Nowcast cho PM2.5
                double currentHourPm25Avg = hourlyData.stream()
                        .mapToDouble(AirQuality::getPm25).average().orElse(0.0);
                double currentHourCoAvg = hourlyData.stream()
                        .mapToDouble(AirQuality::getCo).average().orElse(0.0);
                AverageAirQuality averageAirQuality = AverageAirQuality.builder()
                        .location(location)
                        .averagePm25(currentHourPm25Avg)
                        .averageCo(currentHourCoAvg)
                        .startTime(oneHourAgo)
                        .build();
//                averageAirQualityRepository.save(averageAirQuality);
                List<AverageAirQuality> airQualities = getLatest12AveragePm25(location);
                airQualities.forEach(a -> log.info(String.valueOf(a)));
                double pm25Nowcast = calculateNowcast(airQualities
                        .stream().map(AverageAirQuality::getAveragePm25).toList());
                log.info("Nowcast: {}", pm25Nowcast);
                int pm25Aqi = calculateHourlyAqiMetric(pm25Nowcast, MetricType.PM25);
                log.info("PM25 AQI: {}", pm25Aqi);
                int coAqi = calculateHourlyAqiMetric(currentHourCoAvg, MetricType.CO);

                int compositeAqi = Math.max(pm25Aqi, coAqi);

                AqiSummary hourlyAqi = AqiSummary.builder()
                        .aqi(compositeAqi)
                        .pm25Aqi(pm25Aqi)
                        .coAqi(coAqi)
                        .period(PeriodType.HOUR)
                        .startTime(oneHourAgo)
                        .location(location)
                        .build();
//                aqiSummaryRepository.save(hourlyAqi);
            }
        }
    }

    public List<AverageAirQuality> getLatest12AveragePm25(String location) {
        LocalDateTime fromTime = LocalDateTime.now().minusHours(13);
        return averageAirQualityRepository.findRecentAirQualityByLocation(fromTime,
                location, Sort.by(Sort.Direction.DESC, "startTime"));
    }

}
