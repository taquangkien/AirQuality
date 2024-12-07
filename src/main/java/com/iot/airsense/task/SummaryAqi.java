package com.iot.airsense.task;

import com.iot.airsense.model.*;
import com.iot.airsense.repository.AirQualityRepository;
import com.iot.airsense.repository.AqiSummaryRepository;
import com.iot.airsense.repository.AverageAirQualityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.iot.airsense.util.AqiCalculator.*;

@EnableScheduling
@Configuration
@RequiredArgsConstructor
public class SummaryAqi {
    private final AirQualityRepository airQualityRepository;
    private final AverageAirQualityRepository averageAirQualityRepository;
    private final AqiSummaryRepository aqiSummaryRepository;

    // Lịch tính toán AQI giờ vào hh:00:00 của mỗi giờ
    @Scheduled(cron = "0 0 * * * *")
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
                averageAirQualityRepository.save(averageAirQuality);
                List<AverageAirQuality> airQualities = getLatest12AveragePm25(location);
                double pm25Nowcast = calculateNowcast(airQualities
                        .stream().map(AverageAirQuality::getAveragePm25).toList());
                int pm25Aqi = calculateHourlyAqiMetric(pm25Nowcast, MetricType.PM25);

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
                aqiSummaryRepository.save(hourlyAqi);
            }
        }
    }

    // Lịch tính toán AQI ngày vào 00:01:00
    @Scheduled(cron = "0 1 0 * * *")
    public void calculateDailyAqi() {
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
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
}
