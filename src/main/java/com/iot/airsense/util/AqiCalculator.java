package com.iot.airsense.util;

import com.iot.airsense.model.MetricType;

import java.util.List;

public class AqiCalculator {
    private static final int[] aqiBreakpoints = {0, 50, 100, 150, 200, 300, 400, 500};
    private static final int[] coBreakpoints = {0, 10000, 30000, 45000, 60000, 90000, 120000, 150000};
    private static final int[] pm25Breakpoints = {0, 25, 50, 80, 150, 250, 350, 500};

    public static double calculateNowcast(List<Double> hourlyReadings) {
//        if (hourlyReadings.size() < 3) {
//            throw new IllegalArgumentException("At least 3 hourly readings are required.");
//        }

        double min = hourlyReadings.stream().min(Double::compare).orElse(0.0);
        double max = hourlyReadings.stream().max(Double::compare).orElse(0.0);

//        if (max == 0) return 0;

        double w = (max - min) >= 0.5 * max ? 0.5 : min / max;

        double numerator = 0.0;
        double denominator = 0.0;

        for (int i = 0; i < hourlyReadings.size(); i++) {
            double weight = Math.pow(w, i);
            numerator += hourlyReadings.get(i) * weight;
            denominator += weight;
        }

        return numerator / denominator;
    }

    public static int calculateHourlyAqiMetric(double concentration, MetricType metricType) {
        int[] concentrations;
        if (metricType == MetricType.PM25) {
            concentrations = pm25Breakpoints;
        } else if (metricType == MetricType.CO) {
            concentrations = coBreakpoints;
        } else {
            throw new IllegalArgumentException("Unsupported MetricType: " + metricType);
        }
        for (int i = 1; i < aqiBreakpoints.length; i++) {
            if (concentration <= concentrations[i]) {
                return (int) ((aqiBreakpoints[i] - aqiBreakpoints[i - 1]) * 1.0 / (concentrations[i] - concentrations[i - 1])
                        * (concentration - concentrations[i - 1]) + aqiBreakpoints[i - 1]);
            }
        }
        return 0; // Default case
    }

    public static double calculateDailyConcentration(List<Double> hourlyReadings, MetricType metricType) {
        if (metricType == MetricType.CO) {
            return hourlyReadings.stream().max(Double::compare).orElse(0.0);
        } else if (metricType == MetricType.PM25) {
            return hourlyReadings.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        } else {
            throw new IllegalArgumentException("Unsupported MetricType: " + metricType);
        }
    }

    public static int calculateDailyAqiMetric(List<Double> hourlyReadings, MetricType metricType) {
        double averageConcentration = calculateDailyConcentration(hourlyReadings, metricType);
        return calculateHourlyAqiMetric(averageConcentration, metricType);
    }

    public static int calculateDailyAqiMetric(double averageConcentration, MetricType metricType) {
        return calculateHourlyAqiMetric(averageConcentration, metricType);
    }

}
