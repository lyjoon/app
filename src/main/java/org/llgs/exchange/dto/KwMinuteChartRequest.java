package org.llgs.exchange.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * 키움 주식분봉차트조회요청(ka10080) 요청.
 *
 * @param stkCd 종목코드 (예: 005930, NXT 는 005930_NX)
 * @param ticScope 틱범위 (분 단위: 1, 3, 5, 10, 15, 30, 45, 60)
 * @param baseDt 기준일자 (선택). null 이면 요청에서 제외된다.
 * @param updStkpcTp 수정주가구분 (0: 미적용, 1: 적용)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record KwMinuteChartRequest(
    @JsonProperty("stk_cd") String stkCd,
    @JsonProperty("tic_scope") String ticScope,
    @JsonProperty("base_dt") @JsonFormat(pattern = "yyyyMMdd") LocalDate baseDt,
    @JsonProperty("upd_stkpc_tp") String updStkpcTp) {

  public static KwMinuteChartRequest of(
      String stkCd, int ticScope, LocalDate baseDt, boolean adjustedPrice) {
    return new KwMinuteChartRequest(
        stkCd, String.valueOf(ticScope), baseDt, adjustedPrice ? "1" : "0");
  }
}
