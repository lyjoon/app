package org.llgs.exchange.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.llgs.exchange.constants.KwMarketType;
import org.llgs.exchange.dto.*;
import org.llgs.exchange.exception.KwApiException;
import org.llgs.exchange.properties.KwProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KwRestClientTest {

  /** 호출횟수 제한 검증 외 테스트는 대기 없이 빠르게 수행되도록 넉넉한 값을 사용한다. */
  private static final int DEFAULT_RATE_LIMIT = 1000;

  private DisposableServer server;
  private final AtomicReference<HttpHeaders> receivedHeaders = new AtomicReference<>();
  private final AtomicReference<String> receivedBody = new AtomicReference<>();

  @AfterEach
  void tearDown() {
    if (server != null) {
      server.disposeNow();
    }
  }

  @Test
  void issueToken_정상응답() {
    KwRestClient client =
        startServer(
            "/oauth2/token",
            "{\"expires_dt\":\"20241107083713\",\"token_type\":\"bearer\","
                + "\"token\":\"WQJCwyqInphKnR3bSRtB9NE1lv\",\"return_code\":0,"
                + "\"return_msg\":\"정상적으로 처리되었습니다\"}",
            new HttpHeaders());

    KwTokenResponse response = client.issueToken().block(Duration.ofSeconds(5));

    assertThat(response).isNotNull();
    assertThat(response.token()).isEqualTo("WQJCwyqInphKnR3bSRtB9NE1lv");
    assertThat(response.tokenType()).isEqualTo("bearer");
    assertThat(response.expiresAt()).isEqualTo(LocalDateTime.of(2024, 11, 7, 8, 37, 13));
    assertThat(response.toString()).doesNotContain("WQJCwyqInphKnR3bSRtB9NE1lv");

    assertThat(receivedHeaders.get().getFirst("api-id")).isEqualTo("au10001");
    assertThat(receivedBody.get())
        .contains("\"grant_type\":\"client_credentials\"")
        .contains("\"appkey\":\"test-key\"")
        .contains("\"secretkey\":\"test-secret\"");
  }

  @Test
  void issueToken_return_code가_0이_아니면_예외() {
    KwRestClient client =
        startServer(
            "/oauth2/token",
            "{\"return_code\":3,\"return_msg\":\"인증에 실패했습니다\"}",
            new HttpHeaders());

    assertThatThrownBy(() -> client.issueToken().block(Duration.ofSeconds(5)))
        .isInstanceOf(KwApiException.class)
        .hasMessageContaining("au10001")
        .hasMessageContaining("인증에 실패했습니다")
        .extracting(e -> ((KwApiException) e).getReturnCode())
        .isEqualTo(3);
  }

  @Test
  void getMinuteChart_첫페이지_조회() {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.set("cont-yn", "Y");
    responseHeaders.set("next-key", "NEXT-001");
    KwRestClient client =
        startServer(
            "/api/dostk/chart",
            "{\"stk_cd\":\"005930\",\"stk_min_pole_chart_qry\":["
                + "{\"cur_prc\":\"-78800\",\"trde_qty\":\"7913\",\"cntr_tm\":\"20250917132000\","
                + "\"open_pric\":\"-78850\",\"high_pric\":\"-78900\",\"low_pric\":\"-78800\","
                + "\"pred_pre\":\"-600\",\"pred_pre_sig\":\"5\"}],"
                + "\"return_code\":0,\"return_msg\":\"정상적으로 처리되었습니다\"}",
            responseHeaders);

    KwContinuousResponse<KwMinuteChartResponse> response =
        client
            .getMinuteChart(
                "access-token",
                KwMinuteChartRequest.of("005930", 1, LocalDate.of(2025, 9, 17), true))
            .block(Duration.ofSeconds(5));

    assertThat(response).isNotNull();
    assertThat(response.hasNext()).isTrue();
    assertThat(response.nextKey()).isEqualTo("NEXT-001");
    assertThat(response.body().stkCd()).isEqualTo("005930");
    assertThat(response.body().charts())
        .singleElement()
        .satisfies(
            item -> {
              assertThat(item.cntrTm()).isEqualTo("20250917132000");
              assertThat(item.curPrc()).isEqualTo("-78800");
              assertThat(item.trdeQty()).isEqualTo("7913");
            });

    HttpHeaders headers = receivedHeaders.get();
    assertThat(headers.getFirst("api-id")).isEqualTo("ka10080");
    assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer access-token");
    assertThat(headers.containsHeader("cont-yn")).isFalse();
    assertThat(headers.containsHeader("next-key")).isFalse();
    assertThat(receivedBody.get())
        .contains("\"stk_cd\":\"005930\"")
        .contains("\"tic_scope\":\"1\"")
        .contains("\"base_dt\":\"20250917\"")
        .contains("\"upd_stkpc_tp\":\"1\"");
  }

  @Test
  void getMinuteChart_기준일자가_없으면_base_dt를_보내지_않는다() {
    KwRestClient client =
        startServer(
            "/api/dostk/chart",
            "{\"stk_cd\":\"005930\",\"stk_min_pole_chart_qry\":[],\"return_code\":0}",
            new HttpHeaders());

    client
        .getMinuteChart("access-token", KwMinuteChartRequest.of("005930", 5, null, false))
        .block(Duration.ofSeconds(5));

    assertThat(receivedBody.get()).doesNotContain("base_dt").contains("\"upd_stkpc_tp\":\"0\"");
  }

  @Test
  void getDailyChart_연속조회() {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.set("cont-yn", "N");
    KwRestClient client =
        startServer(
            "/api/dostk/chart",
            "{\"stk_cd\":\"005930\",\"stk_dt_pole_chart_qry\":["
                + "{\"cur_prc\":\"70100\",\"trde_qty\":\"9263135\",\"trde_prica\":\"648525\","
                + "\"dt\":\"20250908\",\"open_pric\":\"69900\",\"high_pric\":\"70400\","
                + "\"low_pric\":\"69600\",\"pred_pre\":\"+600\",\"pred_pre_sig\":\"2\","
                + "\"trde_tern_rt\":\"+0.16\"}],"
                + "\"return_code\":0,\"return_msg\":\"정상적으로 처리되었습니다\"}",
            responseHeaders);

    KwContinuousResponse<KwDailyChartResponse> response =
        client
            .getDailyChart(
                "access-token",
                KwDailyChartRequest.of("005930", LocalDate.of(2025, 9, 8), true),
                "NEXT-001")
            .block(Duration.ofSeconds(5));

    assertThat(response).isNotNull();
    assertThat(response.hasNext()).isFalse();
    assertThat(response.body().charts())
        .singleElement()
        .satisfies(
            item -> {
              assertThat(item.dt()).isEqualTo("20250908");
              assertThat(item.curPrc()).isEqualTo("70100");
              assertThat(item.trdePrica()).isEqualTo("648525");
            });

    HttpHeaders headers = receivedHeaders.get();
    assertThat(headers.getFirst("api-id")).isEqualTo("ka10081");
    assertThat(headers.getFirst("cont-yn")).isEqualTo("Y");
    assertThat(headers.getFirst("next-key")).isEqualTo("NEXT-001");
    assertThat(receivedBody.get())
        .contains("\"stk_cd\":\"005930\"")
        .contains("\"base_dt\":\"20250908\"")
        .contains("\"upd_stkpc_tp\":\"1\"");
  }

  @Test
  void getDailyChart_return_code가_0이_아니면_예외() {
    KwRestClient client =
        startServer(
            "/api/dostk/chart",
            "{\"return_code\":2,\"return_msg\":\"입력 값 오류\"}",
            new HttpHeaders());

    assertThatThrownBy(
            () ->
                client
                    .getDailyChart(
                        "access-token",
                        KwDailyChartRequest.of("005930", LocalDate.of(2025, 9, 8), false))
                    .block(Duration.ofSeconds(5)))
        .isInstanceOf(KwApiException.class)
        .hasMessageContaining("ka10081")
        .hasMessageContaining("입력 값 오류");
  }

  @Test
  void getStockList_camelCase_응답_매핑() {
    KwRestClient client =
        startServer(
            "/api/dostk/stkinfo",
            "{\"list\":[{\"code\":\"005930\",\"name\":\"삼성전자\","
                + "\"listCount\":\"0000005969782550\",\"auditInfo\":\"정상\","
                + "\"regDay\":\"19750611\",\"lastPrice\":\"00070100\",\"state\":\"증거금20%\","
                + "\"marketCode\":\"0\",\"marketName\":\"거래소\",\"upName\":\"전기전자\","
                + "\"upSizeName\":\"대형주\",\"orderWarning\":\"0\",\"nxtEnable\":\"Y\"}],"
                + "\"return_code\":0,\"return_msg\":\"정상적으로 처리되었습니다\"}",
            new HttpHeaders());

    KwContinuousResponse<KwStockListResponse> response =
        client
            .getStockList("access-token", KwStockListRequest.of(KwMarketType.KOSDAQ))
            .block(Duration.ofSeconds(5));

    assertThat(response).isNotNull();
    assertThat(response.hasNext()).isFalse();
    assertThat(response.body().stocks())
        .singleElement()
        .satisfies(
            item -> {
              assertThat(item.code()).isEqualTo("005930");
              assertThat(item.name()).isEqualTo("삼성전자");
              assertThat(item.listCount()).isEqualTo("0000005969782550");
              assertThat(item.regDay()).isEqualTo("19750611");
              assertThat(item.lastPrice()).isEqualTo("00070100");
              assertThat(item.nxtEnable()).isEqualTo("Y");
            });

    assertThat(receivedHeaders.get().getFirst("api-id")).isEqualTo("ka10099");
    assertThat(receivedBody.get()).isEqualTo("{\"mrkt_tp\":\"10\"}");
  }

  @Test
  void getAllStockList_연속조회가_끝날때까지_모든_페이지를_조회한다() {
    List<String> receivedNextKeys = new CopyOnWriteArrayList<>();
    server =
        HttpServer.create()
            .port(0)
            .route(
                routes ->
                    routes.post(
                        "/api/dostk/stkinfo",
                        (request, response) -> {
                          String nextKey = request.requestHeaders().get("next-key");
                          receivedNextKeys.add(String.valueOf(nextKey));
                          String body =
                              nextKey == null
                                  ? "{\"list\":[{\"code\":\"000001\"},{\"code\":\"000002\"}],"
                                      + "\"return_code\":0}"
                                  : "{\"list\":[{\"code\":\"000003\"}],\"return_code\":0}";
                          if (nextKey == null) {
                            response.header("cont-yn", "Y").header("next-key", "PAGE-2");
                          } else {
                            response.header("cont-yn", "N");
                          }
                          return request
                              .receive()
                              .then(
                                  response
                                      .header(
                                          HttpHeaders.CONTENT_TYPE,
                                          "application/json;charset=UTF-8")
                                      .sendString(Mono.just(body), StandardCharsets.UTF_8)
                                      .then());
                        }))
            .bindNow();
    KwRestClient client = newClient();

    List<KwStockListResponse.Item> stocks =
        client
            .getAllStockList("access-token", KwStockListRequest.of(KwMarketType.KOSPI))
            .collectList()
            .block(Duration.ofSeconds(5));

    assertThat(stocks)
        .extracting(KwStockListResponse.Item::code)
        .containsExactly("000001", "000002", "000003");
    assertThat(receivedNextKeys).containsExactly("null", "PAGE-2");
  }

  @Test
  void getDailyChartBetween_기간내_일봉만_연속조회한다() {
    List<String> receivedNextKeys = new CopyOnWriteArrayList<>();
    startPagingServer(
        "/api/dostk/chart",
        receivedNextKeys,
        dailyChartPage("20250910", "20250909", "20250908"),
        dailyChartPage("20250905", "20250904", "20250903"),
        dailyChartPage("20250902", "20250901"));
    KwRestClient client = newClient();

    List<KwDailyChartResponse.Item> charts =
        client
            .getDailyChartBetween(
                "access-token",
                "005930",
                LocalDate.of(2025, 9, 4),
                LocalDate.of(2025, 9, 9),
                true)
            .collectList()
            .block(Duration.ofSeconds(5));

    assertThat(charts)
        .extracting(KwDailyChartResponse.Item::dt)
        .containsExactly("20250909", "20250908", "20250905", "20250904");
    // 시작일자 이전 일봉이 포함된 2페이지에서 연속조회를 멈춘다.
    assertThat(receivedNextKeys).containsExactly("null", "PAGE-2");
    assertThat(receivedBody.get())
        .contains("\"stk_cd\":\"005930\"")
        .contains("\"base_dt\":\"20250909\"")
        .contains("\"upd_stkpc_tp\":\"1\"");
  }

  @Test
  void getDailyChartBetween_시작일자가_종료일자보다_늦으면_예외() {
    KwRestClient client =
        new KwRestClient(
            WebClient.create(),
            new KwProperties(
                "http://localhost",
                "test-key",
                "test-secret",
                new KwProperties.RateLimit(DEFAULT_RATE_LIMIT)));

    assertThatThrownBy(
            () ->
                client
                    .getDailyChartBetween(
                        "access-token",
                        "005930",
                        LocalDate.of(2025, 9, 10),
                        LocalDate.of(2025, 9, 9),
                        false)
                    .blockLast(Duration.ofSeconds(5)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 연속조회는_초당_호출횟수_제한에_맞춰_간격을_두고_호출한다() {
    List<String> receivedNextKeys = new CopyOnWriteArrayList<>();
    List<Long> receivedAtNanos = new CopyOnWriteArrayList<>();
    startPagingServer(
        "/api/dostk/stkinfo",
        receivedNextKeys,
        receivedAtNanos,
        "{\"list\":[{\"code\":\"000001\"}],\"return_code\":0}",
        "{\"list\":[{\"code\":\"000002\"}],\"return_code\":0}",
        "{\"list\":[{\"code\":\"000003\"}],\"return_code\":0}",
        "{\"list\":[{\"code\":\"000004\"}],\"return_code\":0}");
    // 초당 5회 → 호출 간격 200ms
    KwRestClient client = newClient(5);

    List<KwStockListResponse.Item> stocks =
        client
            .getAllStockList("access-token", KwStockListRequest.of(KwMarketType.KOSPI))
            .collectList()
            .block(Duration.ofSeconds(5));

    assertThat(stocks).hasSize(4);
    assertThat(receivedAtNanos).hasSize(4);
    for (int i = 1; i < receivedAtNanos.size(); i++) {
      long intervalMillis =
          Duration.ofNanos(receivedAtNanos.get(i) - receivedAtNanos.get(i - 1)).toMillis();
      // 타이머 오차를 감안해 하한만 느슨하게 검증
      assertThat(intervalMillis).isGreaterThanOrEqualTo(190);
    }
  }

  private static String dailyChartPage(String... dates) {
    String items =
        Arrays.stream(dates)
            .map(dt -> "{\"dt\":\"" + dt + "\",\"cur_prc\":\"70000\"}")
            .collect(Collectors.joining(","));
    return "{\"stk_cd\":\"005930\",\"stk_dt_pole_chart_qry\":[" + items + "],\"return_code\":0}";
  }

  /**
   * 연속조회 응답 서버. {@code pages[i]} 를 응답하며, 마지막 페이지가 아니면 {@code cont-yn=Y, next-key=PAGE-(i+2)} 를
   * 내려준다.
   */
  private void startPagingServer(String path, List<String> receivedNextKeys, String... pages) {
    startPagingServer(path, receivedNextKeys, new CopyOnWriteArrayList<>(), pages);
  }

  private void startPagingServer(
      String path, List<String> receivedNextKeys, List<Long> receivedAtNanos, String... pages) {
    server =
        HttpServer.create()
            .port(0)
            .route(
                routes ->
                    routes.post(
                        path,
                        (request, response) -> {
                          receivedAtNanos.add(System.nanoTime());
                          String nextKey = request.requestHeaders().get("next-key");
                          receivedNextKeys.add(String.valueOf(nextKey));
                          int index =
                              nextKey == null ? 0 : Integer.parseInt(nextKey.substring(5)) - 1;
                          if (index < pages.length - 1) {
                            response.header("cont-yn", "Y").header("next-key", "PAGE-" + (index + 2));
                          } else {
                            response.header("cont-yn", "N");
                          }
                          return request
                              .receive()
                              .aggregate()
                              .asString()
                              .doOnNext(receivedBody::set)
                              .then(
                                  response
                                      .header(
                                          HttpHeaders.CONTENT_TYPE,
                                          "application/json;charset=UTF-8")
                                      .sendString(Mono.just(pages[index]), StandardCharsets.UTF_8)
                                      .then());
                        }))
            .bindNow();
  }

  private KwRestClient startServer(String path, String responseBody, HttpHeaders responseHeaders) {
    server =
        HttpServer.create()
            .port(0)
            .route(
                routes ->
                    routes.post(
                        path,
                        (request, response) -> {
                          HttpHeaders headers = new HttpHeaders();
                          request
                              .requestHeaders()
                              .forEach(e -> headers.add(e.getKey(), e.getValue()));
                          receivedHeaders.set(headers);
                          responseHeaders.forEach(
                              (name, values) -> response.header(name, values.get(0)));
                          return request
                              .receive()
                              .aggregate()
                              .asString()
                              .doOnNext(receivedBody::set)
                              .then(
                                  response
                                      .header(
                                          HttpHeaders.CONTENT_TYPE,
                                          "application/json;charset=UTF-8")
                                      .sendString(Mono.just(responseBody), StandardCharsets.UTF_8)
                                      .then());
                        }))
            .bindNow();
    return newClient();
  }

  private KwRestClient newClient() {
    return newClient(DEFAULT_RATE_LIMIT);
  }

  private KwRestClient newClient(int requestsPerSecond) {
    KwProperties properties =
        new KwProperties(
            "http://localhost:" + server.port(),
            "test-key",
            "test-secret",
            new KwProperties.RateLimit(requestsPerSecond));
    WebClient webClient =
        WebClient.builder()
            .baseUrl(properties.baseUrl())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8")
            .build();
    return new KwRestClient(webClient, properties);
  }
}
