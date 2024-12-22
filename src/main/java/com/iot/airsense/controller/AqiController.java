package com.iot.airsense.controller;

import com.iot.airsense.config.advice.ApiResponse;
import com.iot.airsense.exception.NotFoundException;
import com.iot.airsense.model.*;
import com.iot.airsense.repository.AirQualityRepository;
import com.iot.airsense.repository.AqiSummaryRepository;
import com.iot.airsense.repository.AverageAirQualityRepository;
import com.iot.airsense.service.AqiService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.iot.airsense.util.AqiCalculator.calculateDailyAqiMetric;

@RestController
@Slf4j
@RequestMapping("/api/v1/aqi")
@RequiredArgsConstructor
public class AqiController {
    private final AqiService aqiService;
    private final Map<String, Bucket> ipRateLimiters = new ConcurrentHashMap<>();

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestHourAqi(@RequestParam String location,
                                              HttpServletRequest request) {
        String clientIp = getClientIp(request);
        Bucket bucket = ipRateLimiters.computeIfAbsent(clientIp, k -> createNewBucket());
        if (bucket.tryConsume(1)) {
            return ResponseEntity.ok(aqiService.getLatestHourAqi(location));
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiResponse<>(429,
                "Rate limit exceeded", null));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getAqiSummary(
            @RequestParam String location,
            @RequestParam PeriodType period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            HttpServletRequest request) {
        String clientIp = getClientIp(request);
        Bucket bucket = ipRateLimiters.computeIfAbsent(clientIp, k -> createNewBucket());
        if (bucket.tryConsume(1)) {
            return ResponseEntity.ok(aqiService.getAqiSummary(location, period, start, end));
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiResponse<>(429,
                "Rate limit exceeded", null));
    }

    @GetMapping("/prediction")
    public ResponseEntity<?> getPredictionAqi(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        Bucket bucket = ipRateLimiters.computeIfAbsent(clientIp, k -> createNewBucket());
        if (bucket.tryConsume(1)) {
            return ResponseEntity.ok(aqiService.getPredictionAqi());
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiResponse<>(429,
                "Rate limit exceeded", null));
    }

    private final AirQualityRepository airQualityRepository;
    private final AverageAirQualityRepository averageAirQualityRepository;
    private final AqiSummaryRepository aqiSummaryRepository;

    @GetMapping("/day")
    public void calculateDailyAqi() {
        LocalDateTime now = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime dayStart = now.minusDays(1);

        List<String> locations = airQualityRepository.findDistinctLocations()
                .stream().filter(Objects::nonNull).toList();

        for (String location : locations) {
            List<AverageAirQuality> dailyData = averageAirQualityRepository
                    .findByLocationAndStartTimeBetween(location, dayStart, now.minusSeconds(1));

            if (!dailyData.isEmpty()) {
                // Tính giá trị trung bình 24 giờ cho PM2.5
                double pm25Avg = dailyData.stream()
                        .mapToDouble(AverageAirQuality::getAveragePm25).average().orElse(0);

                // Tính giá trị lớn nhất trong ngày cho CO
                double coMax = dailyData.stream()
                        .mapToDouble(AverageAirQuality::getAverageCo).max().orElse(0);

                // Tính AQI ngày cho từng thông số
                int pm25Aqi = calculateDailyAqiMetric(pm25Avg, MetricType.PM25);
                int coAqi = calculateDailyAqiMetric(coMax, MetricType.CO);

                int compositeAqi = Math.max(pm25Aqi, coAqi);

                AqiSummary dailySummary = AqiSummary.builder()
                        .aqi(compositeAqi)
                        .pm25Aqi(pm25Aqi)
                        .coAqi(coAqi)
                        .period(PeriodType.DAY)
                        .startTime(dayStart)
                        .location(location)
                        .build();

                aqiSummaryRepository.save(dailySummary);
            }
        }
    }

    public List<AverageAirQuality> getLatest12AveragePm25(String location) {
        LocalDateTime fromTime = LocalDateTime.now().minusHours(13);
        return averageAirQualityRepository.findRecentAirQualityByLocation(fromTime,
                location, Sort.by(Sort.Direction.DESC, "startTime"));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            // Trường hợp có nhiều IP trong X-Forwarded-For (proxy chain), lấy IP đầu tiên
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }


    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1))))
                .build();
    }

}
