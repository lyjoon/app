package org.llgs.exchange.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 키움 종목정보 리스트(ka10099) 응답.
 *
 * <p>다른 TR 과 달리 목록 항목은 camelCase 필드로 전달되며, 모든 값은 키움 원문 그대로의 문자열이다. 숫자 항목은 좌측
 * 0-padding 된 문자열로 전달된다.
 *
 * @param stocks 종목정보 목록
 * @param returnCode 응답코드 (0: 정상)
 * @param returnMsg 응답메시지
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KwStockListResponse(
    @JsonProperty("list") List<Item> stocks,
    @JsonProperty("return_code") Integer returnCode,
    @JsonProperty("return_msg") String returnMsg)
    implements KwApiResponse {

  /**
   * 종목정보 항목.
   *
   * @param code 종목코드
   * @param name 종목명
   * @param listCount 상장주식수 (좌측 0-padding)
   * @param auditInfo 감리구분
   * @param regDay 상장일 (yyyyMMdd)
   * @param lastPrice 전일종가 (좌측 0-padding)
   * @param state 종목상태
   * @param marketCode 시장구분코드
   * @param marketName 시장명
   * @param upName 업종명
   * @param upSizeName 회사크기분류
   * @param companyClassName 회사분류
   * @param orderWarning 투자유의종목여부 (0: 해당없음, 1: ETF투자주의, 2: 정리매매, 3: 단기과열, 4: 투자위험, 5: 투자경고)
   * @param nxtEnable NXT 가능여부 (Y: 가능)
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Item(
      @JsonProperty("code") String code,
      @JsonProperty("name") String name,
      @JsonProperty("listCount") String listCount,
      @JsonProperty("auditInfo") String auditInfo,
      @JsonProperty("regDay") String regDay,
      @JsonProperty("lastPrice") String lastPrice,
      @JsonProperty("state") String state,
      @JsonProperty("marketCode") String marketCode,
      @JsonProperty("marketName") String marketName,
      @JsonProperty("upName") String upName,
      @JsonProperty("upSizeName") String upSizeName,
      @JsonProperty("companyClassName") String companyClassName,
      @JsonProperty("orderWarning") String orderWarning,
      @JsonProperty("nxtEnable") String nxtEnable) {}
}
