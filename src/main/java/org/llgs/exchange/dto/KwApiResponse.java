package org.llgs.exchange.dto;

/** 키움 REST-API 응답 공통 필드. */
public interface KwApiResponse {

  /** 응답코드 (0: 정상). */
  Integer returnCode();

  /** 응답메시지. */
  String returnMsg();

  default boolean isSuccess() {
    return returnCode() != null && returnCode() == 0;
  }
}
