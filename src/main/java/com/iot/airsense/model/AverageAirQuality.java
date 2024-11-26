package com.iot.airsense.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "average_air_quality")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AverageAirQuality {
    @Id
    private String id;
    private double averagePm25;
    private double averageCo;
    private String location;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
}
