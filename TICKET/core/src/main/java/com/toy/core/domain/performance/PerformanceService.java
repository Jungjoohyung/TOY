package com.toy.core.domain.performance;

import com.toy.common.exception.EntityNotFoundException;
import com.toy.core.domain.performance.dto.PerformanceDetailResponse;
import com.toy.core.domain.performance.dto.PerformanceResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final PerformanceScheduleRepository scheduleRepository;

    /**
     * 전체 공연 목록 조회 (예매 상태 포함).
     * bookingStatus 가 시각에 따라 변하므로 캐시를 적용하지 않는다.
     */
    public List<PerformanceResponse> getAllPerformances() {
        List<Performance> performances = performanceRepository.findAll();
        List<Long> ids = performances.stream()
                .map(Performance::getId)
                .collect(Collectors.toList());

        // 공연별 가장 이른 스케줄을 한 번의 쿼리로 조회
        Map<Long, PerformanceSchedule> earliestScheduleMap = scheduleRepository
                .findByPerformanceIdIn(ids)
                .stream()
                .filter(s -> s.getBookingStartAt() != null)
                .collect(Collectors.toMap(
                        s -> s.getPerformance().getId(),
                        s -> s,
                        (a, b) -> a.getBookingStartAt().isBefore(b.getBookingStartAt()) ? a : b
                ));

        return performances.stream()
                .map(p -> new PerformanceResponse(p, earliestScheduleMap.get(p.getId())))
                .collect(Collectors.toList());
    }

    public PerformanceDetailResponse getPerformance(Long id) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공연을 찾을 수 없습니다."));

        List<PerformanceSchedule> schedules = scheduleRepository.findByPerformanceId(id);

        return new PerformanceDetailResponse(performance, schedules);
    }
}
