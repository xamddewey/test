package com.xdw.demobackend.service.statistics;

import com.xdw.demobackend.dto.statistics.*;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsService {
    
    List<CategoryStatisticsResponse> getCategoryStatistics(Long ledgerId, LocalDate startDate, LocalDate endDate, Long userId);
    
    List<MemberStatisticsResponse> getMemberStatistics(Long ledgerId, LocalDate startDate, LocalDate endDate, Long userId);
    
    List<TimeRangeStatisticsResponse> getTimeRangeStatistics(
            Long ledgerId, LocalDate startDate, LocalDate endDate, 
            StatisticsRequest.TimeGranularity granularity, Long userId);
    
    OverallStatisticsResponse getOverallStatistics(
            Long ledgerId, LocalDate startDate, LocalDate endDate, 
            StatisticsRequest.TimeGranularity granularity, Long userId);
}
