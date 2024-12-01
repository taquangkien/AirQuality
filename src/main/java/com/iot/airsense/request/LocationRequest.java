package com.iot.airsense.request;

import lombok.Data;

@Data
public class LocationRequest {
    private double latitude;
    private double longitude;
}