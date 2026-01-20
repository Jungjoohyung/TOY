package com.toy.core.domain.performance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceScheduleRepository extends JpaRepository<PerformanceSchedule, Long> {

    List<PerformanceSchedule> findByPerformanceId(Long performanceId);
}