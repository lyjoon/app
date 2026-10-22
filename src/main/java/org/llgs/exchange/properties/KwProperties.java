package org.llgs.exchange.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * 키움 REST-API 연동 설정값.
 *
 * <p>{@code exchange.kw.*} 프로퍼티와 바인딩되며, appKey/appSecret 은 환경변수(KW_APP_KEY,
 * KW_APP_SECRET)로 주입된다.
 */
@ConfigurationProperties(prefix = "exchange.kw")
public record KwProperties(
    @DefaultValue("https://api.kiwoom.com") String baseUrl,
    String appKey,
    String appSecret,
    @DefaultValue RateLimit rateLimit) {

  /** 로그 등에 키 값이 노출되지 않도록 마스킹한다. */
  @Override
  public String toString() {
    return "KwProperties[baseUrl="
        + baseUrl
        + ", appKey=****, appSecret=****, rateLimit="
        + rateLimit
        + "]";
  }

  /**
   * 키움 REST-API 호출횟수 제한 정책.
   *
   * @param requestsPerSecond 초당 최대 호출횟수. 키움 정책 변경 시 이 값만 조정한다.
   */
  public record RateLimit(@DefaultValue("5") int requestsPerSecond) {

    public RateLimit {
      if (requestsPerSecond <= 0) {
        throw new IllegalArgumentException(
            "requestsPerSecond must be positive: " + requestsPerSecond);
      }
    }

    /** 호출 간 최소 간격 (1초 / 초당 최대 호출횟수). */
    public Duration minInterval() {
      return Duration.ofNanos(Duration.ofSeconds(1).toNanos() / requestsPerSecond);
    }
  }
}
