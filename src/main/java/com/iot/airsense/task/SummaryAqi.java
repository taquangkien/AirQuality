package com.iot.airsense.task;

import com.iot.airsense.model.*;
import com.iot.airsense.repository.AirQualityRepository;
import com.iot.airsense.repository.AqiMetricRepository;
import com.iot.airsense.repository.AqiSummaryRepository;
import com.iot.airsense.util.AqiCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.iot.airsense.util.AqiCalculator.*;

@EnableScheduling
@Configuration
@RequiredArgsConstructor
public class SummaryAqi {
    private final AirQualityRepository airQualityRepository;
    private final AqiMetricRepository aqiMetricRepository;
    private final AqiSummaryRepository aqiSummaryRepository;

    // Lịch tính toán AQI giờ vào hh:59:59 của mỗi giờ
    @Scheduled(cron = "59 59 * * * *")
    public void calculateHourlyAqi() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);

        List<String> locations = airQualityRepository.findDistinctLocations();

        for (String location : locations) {
            List<AirQuality> hourlyData = airQualityRepository.findByLocationAndTimestampBetween(location, oneHourAgo, now);
            if (!hourlyData.isEmpty()) {
                // Tính giá trị Nowcast cho PM2.5
                double pm25Nowcast = calculateNowcast(hourlyData.stream().map(AirQuality::getPm25).toList());
                int pm25Aqi = calculateHourlyAqiMetric(pm25Nowcast, MetricType.PM25);

                // Tính AQI của CO
                double coMax = hourlyData.stream().mapToDouble(AirQuality::getCo).max().orElse(0);
                int coAqi = calculateHourlyAqiMetric(coMax, MetricType.CO);

                // Tính AQI giờ tổng hợp
                int compositeAqi = Math.max(pm25Aqi, coAqi);

                // Lưu AQI giờ vào cơ sở dữ liệu
                AqiSummary hourlyAqi = new AqiSummary();
                hourlyAqi.setAqi(compositeAqi);
                hourlyAqi.setPeriod(PeriodType.HOUR);
                hourlyAqi.setStartTime(oneHourAgo);
                hourlyAqi.setLocation(location);

                aqiSummaryRepository.save(hourlyAqi);
            }
        }
    }

    // Lịch tính toán AQI ngày vào 23:59:59
    @Scheduled(cron = "59 59 23 * * *")
    public void calculateDailyAqi() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1).minusNanos(1);

        // Lấy danh sách tất cả các location từ cơ sở dữ liệu
        List<String> locations = airQualityRepository.findDistinctLocations();

        for (String location : locations) {
            // Lấy dữ liệu của location trong ngày
            List<AirQuality> dailyData = airQualityRepository.findByLocationAndTimestampBetween(location, todayStart, todayEnd);

            if (!dailyData.isEmpty()) {
                // Tính giá trị trung bình 24 giờ cho PM2.5
                double pm25Avg = dailyData.stream().mapToDouble(AirQuality::getPm25).average().orElse(0);

                // Tính giá trị lớn nhất trong ngày cho CO
                double coMax = dailyData.stream().mapToDouble(AirQuality::getCo).max().orElse(0);

                // Tính AQI ngày cho từng thông số
                int pm25Aqi = calculateDailyAqiMetric(pm25Avg, MetricType.PM25);
                int coAqi = calculateDailyAqiMetric(coMax, MetricType.CO);

                // Tính AQI ngày tổng hợp
                int compositeAqi = Math.max(pm25Aqi, coAqi);

                // Lưu AQI ngày vào cơ sở dữ liệu
                AqiSummary dailySummary = new AqiSummary();
                dailySummary.setAqi(compositeAqi);
                dailySummary.setPeriod(PeriodType.DAY);
                dailySummary.setStartTime(todayStart);
                dailySummary.setLocation(location);

                aqiSummaryRepository.save(dailySummary);
            }
        }
    }
}
