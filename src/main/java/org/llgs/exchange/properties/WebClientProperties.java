package org.llgs.exchange.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * 외부연동 WebClient 공통 설정값.
 *
 * <p>{@code exchange.webclient.*} 프로퍼티와 바인딩된다.
 */
@ConfigurationProperties(prefix = "exchange.webclient")
public record WebClientProperties(
    @DefaultValue("5s") Duration connectTimeout,
    @DefaultValue("10s") Duration readTimeout,
    @DefaultValue("10s") Duration writeTimeout,
    @DefaultValue("10s") Duration responseTimeout,
    @DefaultValue Pool pool) {

  /** 커넥션 풀 설정값. */
  public record Pool(
      @DefaultValue("exchange-pool") String name,
      @DefaultValue("100") int maxConnections,
      @DefaultValue("5s") Duration pendingAcquireTimeout,
      @DefaultValue("30s") Duration maxIdleTime,
      @DefaultValue("5m") Duration maxLifeTime,
      @DefaultValue("60s") Duration evictInBackground) {}
}
