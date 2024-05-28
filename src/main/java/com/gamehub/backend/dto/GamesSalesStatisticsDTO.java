package com.gamehub.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GamesSalesStatisticsDTO {
    private String gameTitle;
    private Long totalUnitsSold;
    private Double totalRevenue;
}
