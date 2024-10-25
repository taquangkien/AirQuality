package com.iot.airsense.service;

import com.iot.airsense.model.SampleData;
import com.iot.airsense.repository.SampleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SampleService {

    private final SampleRepository repository;

    public List<SampleData> getAllSampleData() {
        return repository.findAll();
    }

    public Optional<SampleData> getSampleDataById(String id) {
        return repository.findById(id);
    }

    public SampleData createSampleData(SampleData sampleData) {
        return repository.save(sampleData);
    }

    public SampleData updateSampleData(String id, SampleData sampleDataDetails) {
        Optional<SampleData> optionalSampleData = repository.findById(id);
        if (optionalSampleData.isPresent()) {
            SampleData sampleData = SampleData.builder().id(id).deviceId(sampleDataDetails.getDeviceId())
                    .qrCodeId(sampleDataDetails.getQrCodeId())
                    .qrCodeValue(sampleDataDetails.getQrCodeValue())
                    .build();
            return repository.save(sampleData);
        }
        return null;
    }

    public void deleteSampleData(String id) {
        repository.deleteById(id);
    }
}
