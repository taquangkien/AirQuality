package com.iot.airsense.service;


import com.iot.airsense.model.Location;
import com.iot.airsense.repository.LocationRepository;
import com.iot.airsense.util.GeoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository; // Repository để lấy vị trí từ DB

    public String getWithinRadiusByLocation(double clientLat, double clientLon, double radiusMeters) {
        // Lấy danh sách tất cả vị trí cố định từ DB
        List<Location> locations = locationRepository.findAll();

        for (Location location : locations) {
            double distance = GeoUtil.calculateDistance(
                    clientLat, clientLon,
                    location.getLatitude(), location.getLongitude()
            );

            // Chuyển đổi bán kính từ mét sang km
            if (distance <= (radiusMeters / 1000.0)) {
                return location.getLocation(); // Có ít nhất một điểm nằm trong bán kính
            }
        }
        return null; // Không có điểm nào trong bán kính
    }
}

