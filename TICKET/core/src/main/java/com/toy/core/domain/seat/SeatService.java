package com.toy.core.domain.seat;

import com.toy.core.domain.seat.dto.SeatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;

    public List<SeatResponse> getSeatsBySchedule(Long scheduleId) {
        return seatRepository.findByScheduleIdOrderBySeatNumberAsc(scheduleId).stream()
                .map(SeatResponse::new)
                .collect(Collectors.toList());
    }
}