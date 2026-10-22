package org.llgs.exchange.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.llgs.exchange.constants.KwMarketType;

/**
 * 키움 종목정보 리스트(ka10099) 요청.
 *
 * @param mrktTp 시장구분 ({@link KwMarketType#getCode()})
 */
public record KwStockListRequest(@JsonProperty("mrkt_tp") String mrktTp) {

  public static KwStockListRequest of(KwMarketType marketType) {
    return new KwStockListRequest(marketType.getCode());
  }
}
