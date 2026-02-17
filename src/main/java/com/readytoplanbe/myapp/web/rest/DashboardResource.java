package com.readytoplanbe.myapp.web.rest;

import com.readytoplanbe.myapp.service.DashboardService;
import com.readytoplanbe.myapp.service.dto.CountByDateDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.jhipster.web.util.ResponseUtil; // Pour gérer les réponses optionnelles si besoin

/**
 * REST controller for admin dashboard data.
 */
@RestController
@RequestMapping("/api/dashboard") // Préfixe pour les endpoints du tableau de bord admin
public class DashboardResource {

    private final DashboardService dashboardService;

    public DashboardResource(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/businessPlansCountByCountry")
    public ResponseEntity<Map<String, Long>> getBusinessPlansCountByCountry() {
        Map<String, Long> data = dashboardService.countBusinessPlansByCountry();
        return ResponseEntity.ok(data);
    }
}
