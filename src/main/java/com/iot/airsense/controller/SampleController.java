package com.iot.airsense.controller;

import com.iot.airsense.model.SampleData;
import com.iot.airsense.service.SampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/sample-data")
@RequiredArgsConstructor
public class SampleController {
    private final SampleService service;

    @GetMapping
    public ResponseEntity<List<SampleData>> getAllSampleData() {
        return ResponseEntity.ok(service.getAllSampleData());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SampleData> getSampleDataById(@PathVariable String id) {
        Optional<SampleData> sampleData = service.getSampleDataById(id);
        return sampleData.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public SampleData createSampleData(@RequestBody SampleData sampleData) {
        return service.createSampleData(sampleData);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SampleData> updateSampleData(@PathVariable String id,
                                                       @RequestBody SampleData sampleDataDetails) {
        SampleData updatedSampleData = service.updateSampleData(id, sampleDataDetails);
        if (updatedSampleData != null) {
            return ResponseEntity.ok(updatedSampleData);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSampleData(@PathVariable String id) {
        service.deleteSampleData(id);
        return ResponseEntity.noContent().build();
    }
}
