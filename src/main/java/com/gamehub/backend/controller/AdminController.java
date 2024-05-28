package com.gamehub.backend.controller;

import com.gamehub.backend.business.PurchaseService;
import com.gamehub.backend.dto.GamesSalesStatisticsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final PurchaseService purchaseService;

    @Autowired
    public AdminController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping("/sales-stats")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<List<GamesSalesStatisticsDTO>> getSalesStatistics(
            @RequestParam(required = false) String gameTitle,
            @RequestParam(required = false, defaultValue = "0") int days) {
        List<GamesSalesStatisticsDTO> stats = purchaseService.getSalesStatistics(gameTitle, days);
        return ResponseEntity.ok(stats);
    }
}
