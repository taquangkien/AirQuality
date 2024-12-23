package com.iot.airsense.controller;

import com.iot.airsense.request.LocationRequest;
import com.iot.airsense.service.AqiService;
import com.iot.airsense.service.LocationService;
import com.iot.airsense.service.NotificationService;
import io.github.jav.exposerversdk.PushClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {
    private final AqiService aqiService;
    private final NotificationService notificationService;
    private final LocationService locationService;
    private String supplierName = "ExponentPushToken[]";

    @PostMapping
    public void processLocation(@RequestBody LocationRequest request) throws PushClientException, InterruptedException {
        log.info(request.toString());

//        String nearestLocation = locationService.getWithinRadiusByLocation(request.getLatitude(),
//                request.getLongitude(), 1000.0);
//        if (nearestLocation != null) {
        int aqi = aqiService.getLatestHourAqi("Khoa's home").getAqi();
//            // Nếu AQI vượt ngưỡng, gửi thông báo
//            if (aqi > 50) {
        String message = String.format("Chất lượng không khí tệ: AQI = %d. Hãy cẩn thận!", aqi);
        log.info(message);
//                notificationService.sendExpoNotification(supplierName, "Cảnh báo AQI", message);
//            }
//        }
    }
}
