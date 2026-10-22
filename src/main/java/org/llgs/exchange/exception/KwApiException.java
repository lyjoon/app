package org.llgs.exchange.exception;

import lombok.Getter;

import java.util.Objects;

/** 키움 REST-API 응답의 return_code 가 정상(0)이 아닐 때 발생하는 예외. */
@Getter
public class KwApiException extends RuntimeException {

  private final String apiId;
  private final Integer returnCode;
  private final String returnMsg;

  public KwApiException(String apiId, Integer returnCode, String returnMsg) {
    super("[" + apiId + "] return_code=" + returnCode + ", return_msg=" + returnMsg);
    this.apiId = apiId;
    this.returnCode = returnCode;
    this.returnMsg = returnMsg;
  }

  public boolean hasError() {
    return !Objects.equals(0, this.returnCode);
  }

}
