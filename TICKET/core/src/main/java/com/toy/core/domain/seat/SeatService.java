package com.toy.core.domain.seat;

import com.toy.common.exception.EntityNotFoundException;
import com.toy.core.domain.performance.PerformanceSchedule;
import com.toy.core.domain.performance.PerformanceScheduleRepository;
import com.toy.core.domain.seat.dto.SeatResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;
    private final PerformanceScheduleRepository scheduleRepository;

    @Cacheable(value = "seats", key = "#scheduleId")
    public List<SeatResponse> getSeatsBySchedule(Long scheduleId) {
        return seatRepository.findByScheduleIdOrderBySeatNumberAsc(scheduleId).stream()
                .map(SeatResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public String createSeat(Long scheduleId, String seatNumber, int price) {
        PerformanceSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("스케줄을 찾을 수 없습니다."));

        Seat seat = Seat.builder()
                .seatNumber(seatNumber)
                .price(price)
                .schedule(schedule)
                .build();

        seatRepository.save(seat);
        return seat.getSeatNumber();
    }
}
