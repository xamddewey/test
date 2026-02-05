package com.xdw.demobackend.controller;

import com.xdw.demobackend.dto.common.ApiResult;
import com.xdw.demobackend.dto.statistics.*;
import com.xdw.demobackend.security.UserPrincipal;
import com.xdw.demobackend.service.statistics.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    
    @GetMapping("/by-category")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<List<CategoryStatisticsResponse>> getStatisticsByCategory(
            @RequestParam Long ledgerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            List<CategoryStatisticsResponse> stats = statisticsService.getCategoryStatistics(
                ledgerId, startDate, endDate, currentUser.getId());
            return ApiResult.success("按类型统计成功", stats);
        } catch (Exception e) {
            log.error("Failed to get category statistics", e);
            return ApiResult.businessError("统计失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/by-member")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<List<MemberStatisticsResponse>> getStatisticsByMember(
            @RequestParam Long ledgerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            List<MemberStatisticsResponse> stats = statisticsService.getMemberStatistics(
                ledgerId, startDate, endDate, currentUser.getId());
            return ApiResult.success("按成员统计成功", stats);
        } catch (Exception e) {
            log.error("Failed to get member statistics", e);
            return ApiResult.businessError("统计失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/by-time")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<List<TimeRangeStatisticsResponse>> getStatisticsByTime(
            @RequestParam Long ledgerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "MONTH") StatisticsRequest.TimeGranularity granularity,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            List<TimeRangeStatisticsResponse> stats = statisticsService.getTimeRangeStatistics(
                ledgerId, startDate, endDate, granularity, currentUser.getId());
            return ApiResult.success("按时间统计成功", stats);
        } catch (Exception e) {
            log.error("Failed to get time-range statistics", e);
            return ApiResult.businessError("统计失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/overall")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<OverallStatisticsResponse> getOverallStatistics(
            @RequestParam Long ledgerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "MONTH") StatisticsRequest.TimeGranularity granularity,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            OverallStatisticsResponse stats = statisticsService.getOverallStatistics(
                ledgerId, startDate, endDate, granularity, currentUser.getId());
            return ApiResult.success("总体统计成功", stats);
        } catch (Exception e) {
            log.error("Failed to get overall statistics", e);
            return ApiResult.businessError("统计失败: " + e.getMessage());
        }
    }
}
