package org.llgs.exchange.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 키움 접근토큰발급(au10001) 요청.
 *
 * @param grantType 고정값 {@code client_credentials}
 * @param appKey 앱키
 * @param secretKey 시크릿키
 */
public record KwTokenRequest(
    @JsonProperty("grant_type") String grantType,
    @JsonProperty("appkey") String appKey,
    @JsonProperty("secretkey") String secretKey) {

  public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

  public static KwTokenRequest of(String appKey, String secretKey) {
    return new KwTokenRequest(GRANT_TYPE_CLIENT_CREDENTIALS, appKey, secretKey);
  }

  /** 로그 등에 키 값이 노출되지 않도록 마스킹한다. */
  @Override
  public String toString() {
    return "KwTokenRequest[grantType=" + grantType + ", appKey=****, secretKey=****]";
  }
}
