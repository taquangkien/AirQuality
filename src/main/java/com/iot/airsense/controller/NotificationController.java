package com.iot.airsense.controller;

import com.iot.airsense.service.NotificationService;
import io.github.jav.exposerversdk.PushClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/sendNotification")
    public ResponseEntity<String> sendNotification(
            @RequestParam String title,
            @RequestParam String body) {

        List<String> supplierNames = Arrays.asList("ExponentPushToken[]" , "ExponentPushToken[]");

        supplierNames.forEach((e)->
                {
                    try {
                        notificationService.sendExpoNotification(e, title, body);
                    } catch (InterruptedException | PushClientException ex) {
                        ex.printStackTrace();
                    }

                }
        );
        return ResponseEntity.ok("Notification sent successfully");

    }
}
