package com.toy.core.domain.performance;

import com.toy.common.exception.EntityNotFoundException;
import com.toy.core.domain.performance.dto.PerformanceDetailResponse;
import com.toy.core.domain.performance.dto.PerformanceResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final PerformanceScheduleRepository scheduleRepository;

    @Cacheable(value = "performances", key = "'all'", cacheManager = "cacheManager")
    public List<PerformanceResponse> getAllPerformances() {
        return performanceRepository.findAll().stream()
                .map(PerformanceResponse::new)
                .collect(Collectors.toList());
    }

    public PerformanceDetailResponse getPerformance(Long id) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공연을 찾을 수 없습니다."));

        List<PerformanceSchedule> schedules = scheduleRepository.findByPerformanceId(id);

        return new PerformanceDetailResponse(performance, schedules);
    }
}
