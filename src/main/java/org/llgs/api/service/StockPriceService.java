package org.llgs.api.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.llgs.api.dto.StockPriceDailyDto;
import org.llgs.api.dto.StockPriceInquiryRequest;
import org.llgs.api.mapper.StockPriceMapper;
import org.llgs.exchange.client.KwRestClient;
import org.llgs.exchange.dto.KwMinuteChartRequest;
import org.llgs.exchange.dto.KwTokenResponse;
import org.llgs.global.util.DateUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockPriceService {

    private final KwRestClient kwRestClient;
    private final StockPriceMapper stockPriceMapper;

    public Flux<StockPriceDailyDto> getStockPrice(@NotNull @Valid StockPriceInquiryRequest stockPriceInquiryRequest) {

        final String stockCode = stockPriceInquiryRequest.stockCode();
        Mono<KwTokenResponse> mono = this.kwRestClient.issueToken();

        return Flux.fromIterable(DateUtil.periodToCollection(stockPriceInquiryRequest.startDate(), stockPriceInquiryRequest.endDate(), true))
                .flatMap(localDate ->
                        mono.flatMap(tokenResponse -> {
                            String token = tokenResponse.token();
                            KwMinuteChartRequest kwMinuteChartRequest = KwMinuteChartRequest.of(stockCode, 1, localDate, false);
                            return this.kwRestClient.getMinuteChart(token, kwMinuteChartRequest);
                        }).map(response -> this.stockPriceMapper.toStockPriceDailyDto(stockCode, localDate, response.body()))
                );
    }
}
