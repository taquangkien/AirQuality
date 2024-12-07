package com.iot.airsense.service;

import com.iot.airsense.exception.NotFoundException;
import com.iot.airsense.model.AqiSummary;
import com.iot.airsense.model.PeriodType;
import com.iot.airsense.repository.AqiSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AqiService {
    private final AqiSummaryRepository repository;

    public AqiSummary getLatestHourAqi(String location) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime previousHourStart = now.minusHours(1).withMinute(0).withSecond(0).withNano(0);
        Optional<AqiSummary> optionalAqiSummary = repository.findByLocationAndStartTime(location, previousHourStart);
        return optionalAqiSummary.orElseThrow(() -> new NotFoundException("No AQI data found for location: "
                + location + " and time: " + previousHourStart));
    }

    public List<AqiSummary> getAqiSummary(String location, PeriodType period,
                                                          LocalDateTime start, LocalDateTime end) {
        return repository.findByLocationAndPeriodAndStartTimeBetween(location, period, start, end);
    }
}
