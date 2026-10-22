package org.llgs.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Getter
public class StockPriceDailyDto implements Comparable<StockPriceDailyDto> {

    private String stockCode;
    private LocalDate baseDate;
    private final Set<StockPriceMinuteDto> stockPriceMinutes = new TreeSet<>();

    public static StockPriceDailyDto of(String stockCode, LocalDate baseDate) {
        StockPriceDailyDto dto = new StockPriceDailyDto();
        dto.stockCode = stockCode;
        dto.baseDate = baseDate;
        return dto;
    }

    public static StockPriceDailyDto of(String stockCode, LocalDate baseDate, Collection<StockPriceMinuteDto> stockPriceMinutes) {
        StockPriceDailyDto dto = of(stockCode, baseDate);
        dto.stockPriceMinutes.addAll(stockPriceMinutes);
        return dto;
    }

    public Long getSupplyDemandIndicate() {
        return stockPriceMinutes.stream().map(StockPriceMinuteDto::getSupplyDemandIndicator).mapToLong(Long::longValue).sum();
    }

    @Override
    @JsonIgnore
    public boolean equals(Object object) {
        if(this == object) return true;
        else {
            if(object instanceof StockPriceDailyDto targetObject) {
                return Objects.equals(this.stockCode, targetObject.getStockCode()) &&
                        Objects.equals(this.baseDate, targetObject.baseDate);
            } else return false;
        }
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        return Objects.hash(this.stockCode, this.baseDate);
    }

    @Override
    @JsonIgnore
    public int compareTo(StockPriceDailyDto targetObject) {
        int naturalOrderValue = this.stockCode.compareTo(targetObject.stockCode);
        if(naturalOrderValue == 0) {
            return this.baseDate.compareTo(targetObject.baseDate);
        } else return naturalOrderValue;
    }
}
