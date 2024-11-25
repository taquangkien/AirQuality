package com.iot.airsense.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "aqi-summary")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AqiSummary {
    @Id
    private String id;
    private int aqi;
    private String location;
    private LocalDateTime startTime;
    private PeriodType period;
}
