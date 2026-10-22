package org.llgs.api.mapper;

import org.llgs.api.dto.StockPriceDailyDto;
import org.llgs.api.dto.StockPriceMinuteDto;
import org.llgs.exchange.dto.KwMinuteChartResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;

@Mapper
public interface StockPriceMapper {

    @Mapping(target = "stockPriceMinutes", source = "response.charts", qualifiedByName = "kwMinuteChartToStockPriceMinuteDto")
    StockPriceDailyDto toStockPriceDailyDto(String stockCode, LocalDate localDate, KwMinuteChartResponse response);

    @Named("kwMinuteChartToStockPriceMinuteDto")
    @Mapping(target = "baseAt", source = "cntrTm", dateFormat = "yyyyMMddHHmmss")
    @Mapping(target = "volume", source = "trdeQty")
    @Mapping(target = "closePrice", source = "curPrc")
    @Mapping(target = "minPrice", source = "lowPric")
    @Mapping(target = "maxPrice", source = "highPric")
    @Mapping(target = "basePrice", source = "openPric")
    StockPriceMinuteDto kwMinuteChartToStockPriceMinuteDto(KwMinuteChartResponse.Item source);
}
