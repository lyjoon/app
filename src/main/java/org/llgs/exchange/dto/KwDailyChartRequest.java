package org.llgs.exchange.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * 키움 주식일봉차트조회요청(ka10081) 요청.
 *
 * @param stkCd 종목코드 (예: 005930, NXT 는 005930_NX)
 * @param baseDt 기준일자 (필수). 기준일자부터 과거 방향으로 조회된다.
 * @param updStkpcTp 수정주가구분 (0: 미적용, 1: 적용)
 */
public record KwDailyChartRequest(
    @JsonProperty("stk_cd") String stkCd,
    @JsonProperty("base_dt") @JsonFormat(pattern = "yyyyMMdd") LocalDate baseDt,
    @JsonProperty("upd_stkpc_tp") String updStkpcTp) {

  public static KwDailyChartRequest of(String stkCd, LocalDate baseDt, boolean adjustedPrice) {
    return new KwDailyChartRequest(stkCd, baseDt, adjustedPrice ? "1" : "0");
  }
}
