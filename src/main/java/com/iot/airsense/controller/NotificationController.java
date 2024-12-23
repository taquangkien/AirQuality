package com.iot.airsense.controller;

import com.iot.airsense.config.advice.ApiResponse;
import com.iot.airsense.service.AqiService;
import com.iot.airsense.service.NotificationService;
import io.github.jav.exposerversdk.PushClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final AqiService aqiService;

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(
            @RequestParam String title,
            @RequestParam String body) {
//        List<String> supplierNames = Arrays.asList("ExponentPushToken[XJ-vcHH450tXqbVT6nmcLy]" , "ExponentPushToken[]");
        List<String> supplierNames = List.of("ExponentPushToken[XJ-vcHH450tXqbVT6nmcLy]");

        supplierNames.forEach((e)->
                {
                    try {
                        notificationService.sendExpoNotification(e, title, body);
                    } catch (InterruptedException | PushClientException ex) {
                        ex.printStackTrace();
                    }

                }
        );
        return ResponseEntity.ok(new ApiResponse<>(200, "Notification sent successfully", null));
    }
}
