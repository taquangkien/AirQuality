package com.iot.airsense.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "sample")
public class SampleData {
    @Id
    private String id;
    private String deviceId;
    private String qrCodeId;
    private String qrCodeValue;
}
