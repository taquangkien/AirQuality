package com.iot.airsense.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "aqi-summary")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AqiSummary {
    @Id
    private String id;
    private int aqi;
    private String location;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    private PeriodType period;
}
