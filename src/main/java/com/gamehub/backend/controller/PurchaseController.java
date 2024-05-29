package com.gamehub.backend.controller;

import com.gamehub.backend.business.PurchaseService;
import com.gamehub.backend.domain.Purchase;
import com.gamehub.backend.dto.PurchaseDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/purchases")
@CrossOrigin(origins = "http://localhost:5173")
@Validated
public class PurchaseController {
    private final PurchaseService purchaseService;

    @Autowired
    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping("/{userId}/game/{gameId}")
    @PreAuthorize("#userId == principal.id")
    public ResponseEntity<?> purchaseGame(@PathVariable Long userId, @PathVariable Long gameId) {
        try {
            Purchase purchase = purchaseService.purchaseGame(userId, gameId);
            PurchaseDTO purchaseDTO = new PurchaseDTO(
                    purchase.getId(),
                    purchase.getGame().getTitle(),
                    purchase.getAmount(),
                    purchase.getPurchaseDate()
            );
            return ResponseEntity.ok(purchaseDTO);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    @PreAuthorize("#userId == principal.id")
    public ResponseEntity<List<PurchaseDTO>> getPurchases(
            @PathVariable Long userId,
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date fromDate,
            @RequestParam(value = "minAmount", required = false) Double minAmount,
            @RequestParam(value = "maxAmount", required = false) Double maxAmount) {
        List<PurchaseDTO> purchases = purchaseService.getPurchases(userId, fromDate, minAmount, maxAmount);
        return ResponseEntity.ok(purchases);
    }

    @GetMapping("/owns")
    @PreAuthorize("#userId == principal.id")
    public ResponseEntity<Boolean> checkOwnership(@RequestParam Long userId, @RequestParam Long gameId) {
        boolean ownsGame = purchaseService.checkOwnership(userId, gameId);
        return ResponseEntity.ok(ownsGame);
    }
}
