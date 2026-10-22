package org.llgs.exchange.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 키움 종목정보 리스트(ka10099) 시장구분({@code mrkt_tp}). */
@Getter
@RequiredArgsConstructor
public enum KwMarketType {
  KOSPI("0", "코스피"),
  KOSDAQ("10", "코스닥"),
  INFRA_FUND("2", "인프라투융자"),
  ELW("3", "ELW"),
  MUTUAL_FUND("4", "뮤추얼펀드"),
  WARRANT("5", "신주인수권"),
  REITS("6", "리츠종목"),
  WARRANT_CERTIFICATE("7", "신주인수권증서"),
  ETF("8", "ETF"),
  HIGH_YIELD_FUND("9", "하이일드펀드"),
  K_OTC("30", "K-OTC"),
  KONEX("50", "코넥스"),
  ETN("60", "ETN"),
  LOSS_LIMIT_ETN("70", "손실제한 ETN"),
  GOLD_SPOT("80", "금현물"),
  VOLATILITY_ETN("90", "변동성 ETN");

  private final String code;
  private final String description;
}
