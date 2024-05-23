package com.gamehub.backend.business;

import com.gamehub.backend.domain.Purchase;
import com.gamehub.backend.dto.PurchaseDTO;

import java.util.Date;
import java.util.List;

public interface PurchaseService {
    Purchase purchaseGame(Long userId, Long gameId);
    List<PurchaseDTO> getPurchases(Long userId, Date fromDate, Double minAmount, Double maxAmount);
    boolean checkOwnership(Long userId, Long gameId);
}
