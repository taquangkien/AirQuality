package com.iot.airsense.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "air_quality")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AirQuality {
    @Id
    private String id;
    private String deviceId;
    private Double co;
    private Double pm25;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String location;
}