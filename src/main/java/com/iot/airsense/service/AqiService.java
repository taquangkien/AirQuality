package com.iot.airsense.service;

import com.iot.airsense.model.AqiSummary;
import com.iot.airsense.repository.AqiSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AqiService {
    private final AqiSummaryRepository repository;

    public int getLatestHourAqi(String location) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime previousHourStart = now.minusHours(1).withMinute(0).withSecond(0).withNano(0);
        Optional<AqiSummary> optionalAqiSummary = repository.findByLocationAndStartTime(location, previousHourStart);
        return optionalAqiSummary.map(AqiSummary::getAqi).orElse(0);
    }
}
