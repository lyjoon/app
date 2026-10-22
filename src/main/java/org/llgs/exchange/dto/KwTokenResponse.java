package org.llgs.exchange.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 키움 접근토큰발급(au10001) 응답.
 *
 * @param expiresDt 만료일시 (yyyyMMddHHmmss)
 * @param tokenType 토큰타입 (bearer)
 * @param token 접근토큰
 * @param returnCode 응답코드 (0: 정상)
 * @param returnMsg 응답메시지
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KwTokenResponse(
    @JsonProperty("expires_dt") String expiresDt,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("token") String token,
    @JsonProperty("return_code") Integer returnCode,
    @JsonProperty("return_msg") String returnMsg)
    implements KwApiResponse {

  private static final DateTimeFormatter EXPIRES_DT_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  /** 만료일시를 {@link LocalDateTime} 으로 변환한다. */
  public LocalDateTime expiresAt() {
    return expiresDt == null ? null : LocalDateTime.parse(expiresDt, EXPIRES_DT_FORMAT);
  }

  /** 로그 등에 토큰 값이 노출되지 않도록 마스킹한다. */
  @Override
  public String toString() {
    return "KwTokenResponse[expiresDt="
        + expiresDt
        + ", tokenType="
        + tokenType
        + ", token=****, returnCode="
        + returnCode
        + ", returnMsg="
        + returnMsg
        + "]";
  }
}
