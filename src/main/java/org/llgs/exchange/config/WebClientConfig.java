package org.llgs.exchange.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.llgs.exchange.properties.KwProperties;
import org.llgs.exchange.properties.WebClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.util.concurrent.TimeUnit;

/**
 * 외부연동용 WebClient 설정.
 *
 * <p>공통 커넥션 풀/타임아웃이 적용된 {@link ClientHttpConnector} 를 등록하여 Spring Boot 가 제공하는 {@link
 * WebClient.Builder} 에 반영되도록 하고, 연동 대상별 WebClient 를 빈으로 등록한다.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({WebClientProperties.class, KwProperties.class})
public class WebClientConfig {

  private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

  private static final MediaType APPLICATION_JSON_UTF8 =
      MediaType.parseMediaType("application/json;charset=UTF-8");

  @Bean(destroyMethod = "dispose")
  ConnectionProvider exchangeConnectionProvider(WebClientProperties properties) {
    WebClientProperties.Pool pool = properties.pool();
    return ConnectionProvider.builder(pool.name())
        .maxConnections(pool.maxConnections())
        .pendingAcquireTimeout(pool.pendingAcquireTimeout())
        .maxIdleTime(pool.maxIdleTime())
        .maxLifeTime(pool.maxLifeTime())
        .evictInBackground(pool.evictInBackground())
        .build();
  }

  @Bean
  ClientHttpConnector exchangeClientHttpConnector(
      ConnectionProvider exchangeConnectionProvider, WebClientProperties properties) {
    long readTimeoutMillis = properties.readTimeout().toMillis();
    long writeTimeoutMillis = properties.writeTimeout().toMillis();

    HttpClient httpClient =
        HttpClient.create(exchangeConnectionProvider)
            .option(
                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                Math.toIntExact(properties.connectTimeout().toMillis()))
            .responseTimeout(properties.responseTimeout())
            .doOnConnected(
                connection ->
                    connection
                        .addHandlerLast(
                            new ReadTimeoutHandler(readTimeoutMillis, TimeUnit.MILLISECONDS))
                        .addHandlerLast(
                            new WriteTimeoutHandler(writeTimeoutMillis, TimeUnit.MILLISECONDS)));

    return new ReactorClientHttpConnector(httpClient);
  }

  /** 키움 REST-API 연동용 WebClient. */
  @Bean
  WebClient kwWebClient(WebClient.Builder webClientBuilder, KwProperties properties) {
    return webClientBuilder
        .clone()
        .baseUrl(properties.baseUrl())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8.toString())
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .filter(logRequest())
        .filter(logResponse())
        .build();
  }

  /** 요청 로그. 인증 토큰 등이 노출되지 않도록 헤더는 남기지 않는다. */
  static ExchangeFilterFunction logRequest() {
    return ExchangeFilterFunction.ofRequestProcessor(
        request -> {
          log.debug(">>> {} {}", request.method(), request.url());
          return Mono.just(request);
        });
  }

  static ExchangeFilterFunction logResponse() {
    return ExchangeFilterFunction.ofResponseProcessor(
        response -> {
          log.debug("<<< {}", response.statusCode());
          return Mono.just(response);
        });
  }
}
