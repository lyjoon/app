package org.llgs.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record StockPriceInquiryRequest (
        @NotNull String stockCode,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate){
}
