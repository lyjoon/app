package org.llgs.exchange.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.llgs.exchange.properties.KwProperties;
import org.llgs.exchange.properties.WebClientProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class WebClientConfigTest {

  private final WebClientConfig config = new WebClientConfig();

  private DisposableServer server;
  private ConnectionProvider connectionProvider;
  private final AtomicReference<HttpHeaders> receivedHeaders = new AtomicReference<>();

  @BeforeEach
  void setUp() {
    server =
        HttpServer.create()
            .port(0)
            .route(
                routes ->
                    routes.get(
                        "/ping",
                        (request, response) -> {
                          HttpHeaders headers = new HttpHeaders();
                          request.requestHeaders().forEach(e -> headers.add(e.getKey(), e.getValue()));
                          receivedHeaders.set(headers);
                          return response.sendString(Mono.just("pong"));
                        }))
            .bindNow();
  }

  @AfterEach
  void tearDown() {
    server.disposeNow();
    if (connectionProvider != null) {
      connectionProvider.dispose();
    }
  }

  @Test
  void kwWebClient_baseUrl_defaultHeader() {
    WebClientProperties properties = defaultProperties();
    connectionProvider = config.exchangeConnectionProvider(properties);
    ClientHttpConnector connector =
        config.exchangeClientHttpConnector(connectionProvider, properties);
    KwProperties kwProperties =
        new KwProperties(
            "http://localhost:" + server.port(), "key", "secret", new KwProperties.RateLimit(5));

    WebClient webClient =
        config.kwWebClient(WebClient.builder().clientConnector(connector), kwProperties);

    String body =
        webClient.get().uri("/ping").retrieve().bodyToMono(String.class).block(Duration.ofSeconds(5));

    assertThat(body).isEqualTo("pong");
    assertThat(receivedHeaders.get().getFirst(HttpHeaders.CONTENT_TYPE))
        .isEqualTo("application/json;charset=UTF-8");
    assertThat(receivedHeaders.get().getFirst(HttpHeaders.ACCEPT)).isEqualTo("application/json");
  }

  @Test
  void kwProperties_toString은_키를_마스킹한다() {
    KwProperties properties =
        new KwProperties(
            "https://api.kiwoom.com", "real-key", "real-secret", new KwProperties.RateLimit(5));

    assertThat(properties.toString()).doesNotContain("real-key", "real-secret").contains("****");
  }

  private static WebClientProperties defaultProperties() {
    return new WebClientProperties(
        Duration.ofSeconds(5),
        Duration.ofSeconds(10),
        Duration.ofSeconds(10),
        Duration.ofSeconds(10),
        new WebClientProperties.Pool(
            "test-pool",
            10,
            Duration.ofSeconds(5),
            Duration.ofSeconds(30),
            Duration.ofMinutes(5),
            Duration.ofSeconds(60)));
  }
}
