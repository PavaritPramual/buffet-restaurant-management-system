package com.buffetrestaurant.service;

import com.buffetrestaurant.dto.response.DashboardResponse;
import java.time.LocalDate;

public interface DashboardService {
    DashboardResponse summarize(LocalDate startDate, LocalDate endDate);
}
