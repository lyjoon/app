package org.llgs.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.llgs.api.dto.StockPriceDailyDto;
import org.llgs.api.dto.StockPriceInquiryRequest;
import org.llgs.api.service.StockPriceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StockPriceController {

    private final StockPriceService stockPriceService;

    @GetMapping(value = "/daily-stock-prices")
    public Flux<StockPriceDailyDto> getDailyStockPrices(StockPriceInquiryRequest stockPriceInquiryRequest) {
        log.debug("{}", stockPriceInquiryRequest);
        return stockPriceService.getStockPrice(stockPriceInquiryRequest);
    }
}
