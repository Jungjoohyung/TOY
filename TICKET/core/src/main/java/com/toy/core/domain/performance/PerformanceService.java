package com.toy.core.domain.performance;

import com.toy.core.domain.performance.dto.PerformanceResponse;
import com.toy.core.domain.performance.PerformanceScheduleRepository;
import com.toy.core.domain.performance.dto.PerformanceDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 조회 전용이라 성능 최적화
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final PerformanceScheduleRepository scheduleRepository;
    

    // 1. 공연 전체 목록 조회
    @Cacheable(value = "performances", key = "'all'", cacheManager = "cacheManager") 
    @Transactional(readOnly = true)
    public List<PerformanceResponse> getAllPerformances() {
        return performanceRepository.findAll().stream()
                .map(PerformanceResponse::new) // Entity -> DTO 변환
                .collect(Collectors.toList());
    }

    // [수정] 상세 조회: 공연 정보 + 스케줄 목록까지 같이 줌
    public PerformanceDetailResponse getPerformance(Long id) {
        // 1. 공연 찾기
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공연을 찾을 수 없습니다."));

        // 2. 해당 공연의 스케줄 찾기 (JPA 쿼리 메서드 필요)
        // 잠깐! 아래 findByPerformanceId가 없어서 에러날 수 있음 -> Repository에 추가해야 함
        List<PerformanceSchedule> schedules = scheduleRepository.findByPerformanceId(id);

        return new PerformanceDetailResponse(performance, schedules);
    }
}