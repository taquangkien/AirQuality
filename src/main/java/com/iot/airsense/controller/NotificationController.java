package com.iot.airsense.controller;

import com.iot.airsense.config.advice.ApiResponse;
import com.iot.airsense.service.NotificationService;
import io.github.jav.exposerversdk.PushClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

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
