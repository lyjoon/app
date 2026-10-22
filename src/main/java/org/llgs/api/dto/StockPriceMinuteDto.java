package org.llgs.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
public class StockPriceMinuteDto implements Comparable<StockPriceMinuteDto> {

    private LocalDateTime baseAt;

    private Long volume;

    private Integer basePrice;

    private Integer maxPrice;

    private Integer minPrice;

    private Integer closePrice;

    public long getSupplyDemandIndicator() {
        int o = this.basePrice;
        int c = this.closePrice;
        long v = this.volume;
        return (o < c ? c * v : 0) - (o > c ? c * v : 0);
    }

    @Override
    @JsonIgnore
    public boolean equals(Object object) {
        if(this == object) return true;
        else {
            if(object instanceof StockPriceMinuteDto dto) {
                return Objects.equals(this.baseAt, dto.baseAt);
            } else return false;
        }
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        return Objects.hash(baseAt);
    }

    @Override
    @JsonIgnore
    public int compareTo(StockPriceMinuteDto o) {
        return this.baseAt.compareTo(o.baseAt);
    }

}
