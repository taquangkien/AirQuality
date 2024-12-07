package com.iot.airsense.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iot.airsense.model.AirQuality;
import com.iot.airsense.repository.AirQualityRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttSubscriber {
    private final MqttClient mqttClient;
    private final AirQualityRepository repository;
    private final ObjectMapper objectMapper;
    @Value("${mqtt.topic}")
    private String topic;

//    @PostConstruct
//    public void subscribeToTopic() {
//        try {
//            // Đăng ký callback
//            mqttClient.setCallback(new MqttCallback() {
//                @Override
//                public void connectionLost(Throwable cause) {
//                    System.out.println("Connection lost: " + cause.getMessage());
//                }
//
//                @Override
//                public void messageArrived(String topic, MqttMessage message) {
//                    String payload = new String(message.getPayload());
//                    System.out.println("Received message: " + payload);
//                    processMessage(payload);
//                }
//
//                @Override
//                public void deliveryComplete(IMqttDeliveryToken token) {
//                    // Không dùng cho Subscribe
//                }
//            });
//
//            mqttClient.subscribe(topic);
//            System.out.println("Subscribed to topic: " + topic);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @PostConstruct
    public void subscribeToTopic() {
        try {
            mqttClient.subscribe(topic, (topic, message) -> {
                String payload = new String(message.getPayload());
                log.info("Received message from topic " + topic + ": " + payload);
                processMessage(payload);
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void processMessage(String payload) {
        objectMapper.registerModule(new JavaTimeModule());
        try {
            AirQuality data = objectMapper.readValue(payload, AirQuality.class);
            repository.save(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}