package org.llgs.exchange.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 키움 주식분봉차트조회요청(ka10080) 응답.
 *
 * <p>가격 항목은 키움 원문 그대로 부호(+/-)가 붙은 문자열로 전달된다.
 *
 * @param stkCd 종목코드
 * @param charts 주식분봉차트조회 목록
 * @param returnCode 응답코드 (0: 정상)
 * @param returnMsg 응답메시지
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KwMinuteChartResponse(
    @JsonProperty("stk_cd") String stkCd,
    @JsonProperty("stk_min_pole_chart_qry") List<Item> charts,
    @JsonProperty("return_code") Integer returnCode,
    @JsonProperty("return_msg") String returnMsg)
    implements KwApiResponse {

  /**
   * 분봉 항목.
   *
   * @param curPrc 현재가(종가)
   * @param trdeQty 거래량
   * @param cntrTm 체결시간 (yyyyMMddHHmmss)
   * @param openPric 시가
   * @param highPric 고가
   * @param lowPric 저가
   * @param predPre 전일대비
   * @param predPreSig 전일대비기호 (1: 상한, 2: 상승, 3: 보합, 4: 하한, 5: 하락)
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Item(
      @JsonProperty("cur_prc") String curPrc,
      @JsonProperty("trde_qty") String trdeQty,
      @JsonProperty("cntr_tm") String cntrTm,
      @JsonProperty("open_pric") String openPric,
      @JsonProperty("high_pric") String highPric,
      @JsonProperty("low_pric") String lowPric,
      @JsonProperty("pred_pre") String predPre,
      @JsonProperty("pred_pre_sig") String predPreSig) {}
}
