package com.iot.airsense.util;

public class GeoUtil {
    private static final double EARTH_RADIUS_KM = 6371.0; // Bán kính Trái Đất tính bằng km

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Chuyển đổi độ sang radian
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        // Áp dụng công thức haversine
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Khoảng cách tính bằng km
        return EARTH_RADIUS_KM * c;
    }
}
