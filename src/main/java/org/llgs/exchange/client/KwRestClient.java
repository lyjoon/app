package org.llgs.exchange.client;

import org.llgs.exchange.dto.*;
import org.llgs.exchange.exception.KwApiException;
import org.llgs.exchange.properties.KwProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 키움 REST-API 클라이언트.
 *
 * <p>키움 REST-API 는 요청 헤더 {@code api-id} 로 TR 을 구분한다.
 */
@Component
public class KwRestClient {

  static final String HEADER_API_ID = "api-id";
  static final String HEADER_CONT_YN = "cont-yn";
  static final String HEADER_NEXT_KEY = "next-key";

  static final String TOKEN_API_ID = "au10001";
  static final String TOKEN_URI = "/oauth2/token";

  static final String MINUTE_CHART_API_ID = "ka10080";
  static final String DAILY_CHART_API_ID = "ka10081";
  static final String CHART_URI = "/api/dostk/chart";

  static final String STOCK_LIST_API_ID = "ka10099";
  static final String STOCK_INFO_URI = "/api/dostk/stkinfo";

  private final WebClient kwWebClient;
  private final KwProperties kwProperties;

  /** 다음 호출이 허용되는 시각({@link System#nanoTime()} 기준). */
  private final AtomicLong nextPermitNanos = new AtomicLong(System.nanoTime());

  public KwRestClient(@Qualifier("kwWebClient") WebClient kwWebClient, KwProperties kwProperties) {
    this.kwWebClient = kwWebClient;
    this.kwProperties = kwProperties;
  }

  /**
   * 접근토큰발급(au10001).
   *
   * <p>설정된 appKey/appSecret 으로 접근토큰을 발급받는다. 응답의 {@code return_code} 가 0 이 아니면 {@link
   * KwApiException} 으로 종료된다.
   */
  public Mono<KwTokenResponse> issueToken() {
    return kwWebClient
        .post()
        .uri(TOKEN_URI)
        .header(HEADER_API_ID, TOKEN_API_ID)
        .bodyValue(KwTokenRequest.of(kwProperties.appKey(), kwProperties.appSecret()))
        .retrieve()
        .bodyToMono(KwTokenResponse.class)
        .flatMap(response -> validate(TOKEN_API_ID, response));
  }

  /** 주식분봉차트조회요청(ka10080). 첫 페이지를 조회한다. */
  public Mono<KwContinuousResponse<KwMinuteChartResponse>> getMinuteChart(
      String accessToken, KwMinuteChartRequest request) {
    return getMinuteChart(accessToken, request, null);
  }

  /**
   * 주식분봉차트조회요청(ka10080).
   *
   * @param accessToken 접근토큰 ({@link #issueToken()} 으로 발급)
   * @param request 요청 조건
   * @param nextKey 연속조회키. 이전 응답의 {@link KwContinuousResponse#nextKey()} 를 전달하며, null 이면 첫 페이지를
   *     조회한다.
   */
  public Mono<KwContinuousResponse<KwMinuteChartResponse>> getMinuteChart(
      String accessToken, KwMinuteChartRequest request, String nextKey) {
    return postContinuous(
        MINUTE_CHART_API_ID,
        CHART_URI,
        accessToken,
        nextKey,
        request,
        KwMinuteChartResponse.class);
  }

  /** 주식일봉차트조회요청(ka10081). 첫 페이지를 조회한다. */
  public Mono<KwContinuousResponse<KwDailyChartResponse>> getDailyChart(
      String accessToken, KwDailyChartRequest request) {
    return getDailyChart(accessToken, request, null);
  }

  /**
   * 주식일봉차트조회요청(ka10081).
   *
   * @param accessToken 접근토큰 ({@link #issueToken()} 으로 발급)
   * @param request 요청 조건
   * @param nextKey 연속조회키. 이전 응답의 {@link KwContinuousResponse#nextKey()} 를 전달하며, null 이면 첫 페이지를
   *     조회한다.
   */
  public Mono<KwContinuousResponse<KwDailyChartResponse>> getDailyChart(
      String accessToken, KwDailyChartRequest request, String nextKey) {
    return postContinuous(
        DAILY_CHART_API_ID, CHART_URI, accessToken, nextKey, request, KwDailyChartResponse.class);
  }

  /**
   * 주식일봉차트조회요청(ka10081) 기간 조회.
   *
   * <p>{@code toDate} 를 기준일자로 조회한 뒤 {@code fromDate} 이전 일자가 나올 때까지 연속조회하여, 기간 내 일봉만
   * 발행한다. 키움 응답 순서 그대로 최신 일자부터 과거 방향(내림차순)으로 발행된다.
   *
   * @param accessToken 접근토큰 ({@link #issueToken()} 으로 발급)
   * @param stkCd 종목코드
   * @param fromDate 조회 시작일자 (포함)
   * @param toDate 조회 종료일자 (포함)
   * @param adjustedPrice 수정주가 적용 여부
   */
  public Flux<KwDailyChartResponse.Item> getDailyChartBetween(
      String accessToken,
      String stkCd,
      LocalDate fromDate,
      LocalDate toDate,
      boolean adjustedPrice) {
    if (fromDate.isAfter(toDate)) {
      return Flux.error(
          new IllegalArgumentException(
              "fromDate(" + fromDate + ") must not be after toDate(" + toDate + ")"));
    }
    KwDailyChartRequest request = KwDailyChartRequest.of(stkCd, toDate, adjustedPrice);
    String from = fromDate.format(DateTimeFormatter.BASIC_ISO_DATE);
    String to = toDate.format(DateTimeFormatter.BASIC_ISO_DATE);

    return fetchAllPages(
            nextKey -> getDailyChart(accessToken, request, nextKey),
            response -> !reachedDate(response.body(), from))
        .concatMapIterable(
            response -> response.body().charts() == null ? List.of() : response.body().charts())
        .filter(item -> item.dt() != null && item.dt().compareTo(to) <= 0)
        .takeWhile(item -> item.dt().compareTo(from) >= 0);
  }

  /** 응답의 마지막(가장 과거) 일봉이 {@code date}(yyyyMMdd) 이전이거나 데이터가 없으면 true. */
  private static boolean reachedDate(KwDailyChartResponse body, String date) {
    List<KwDailyChartResponse.Item> charts = body.charts();
    if (charts == null || charts.isEmpty()) {
      return true;
    }
    String oldestDt = charts.get(charts.size() - 1).dt();
    return oldestDt == null || oldestDt.compareTo(date) < 0;
  }

  /** 종목정보 리스트(ka10099). 첫 페이지를 조회한다. */
  public Mono<KwContinuousResponse<KwStockListResponse>> getStockList(
      String accessToken, KwStockListRequest request) {
    return getStockList(accessToken, request, null);
  }

  /**
   * 종목정보 리스트(ka10099).
   *
   * @param accessToken 접근토큰 ({@link #issueToken()} 으로 발급)
   * @param request 요청 조건
   * @param nextKey 연속조회키. 이전 응답의 {@link KwContinuousResponse#nextKey()} 를 전달하며, null 이면 첫 페이지를
   *     조회한다.
   */
  public Mono<KwContinuousResponse<KwStockListResponse>> getStockList(
      String accessToken, KwStockListRequest request, String nextKey) {
    return postContinuous(
        STOCK_LIST_API_ID,
        STOCK_INFO_URI,
        accessToken,
        nextKey,
        request,
        KwStockListResponse.class);
  }

  /**
   * 종목정보 리스트(ka10099) 전체 조회.
   *
   * <p>연속조회({@code cont-yn=Y})가 끝날 때까지 다음 페이지를 이어서 조회하고, 모든 페이지의 종목을 순서대로 발행한다.
   */
  public Flux<KwStockListResponse.Item> getAllStockList(
      String accessToken, KwStockListRequest request) {
    return fetchAllPages(nextKey -> getStockList(accessToken, request, nextKey), response -> true)
        .concatMapIterable(
            response ->
                response.body().stocks() == null ? List.of() : response.body().stocks());
  }

  /**
   * 연속조회 공통 처리.
   *
   * <p>첫 페이지부터 {@code cont-yn=Y} 이고 {@code continueIf} 가 true 인 동안 다음 페이지를 이어서 조회한다. 매 페이지 요청
   * 전 {@link #acquirePermit()} 로 호출 간격을 확인하여, 키움 호출횟수 제한({@link KwProperties.RateLimit})을 넘지 않도록
   * 필요한 시간만큼 대기한다.
   *
   * @param pageFetcher 연속조회키(첫 페이지는 null)로 한 페이지를 조회하는 함수
   * @param continueIf 다음 페이지 조회 여부 추가 조건
   */
  private <T> Flux<KwContinuousResponse<T>> fetchAllPages(
      Function<String, Mono<KwContinuousResponse<T>>> pageFetcher,
      Predicate<KwContinuousResponse<T>> continueIf) {
    Function<String, Mono<KwContinuousResponse<T>>> throttledFetcher =
        nextKey -> acquirePermit().then(Mono.defer(() -> pageFetcher.apply(nextKey)));
    return throttledFetcher
        .apply(null)
        .expand(
            response ->
                response.hasNext() && continueIf.test(response)
                    ? throttledFetcher.apply(response.nextKey())
                    : Mono.empty());
  }

  /**
   * 호출횟수 제한 확인.
   *
   * <p>직전 호출 시각과 현재 시각을 비교해 최소 호출 간격({@link KwProperties.RateLimit#minInterval()})이 지나지 않았으면
   * 남은 시간만큼 대기한 뒤 완료된다. 호출 시각은 클라이언트 단위로 관리되므로, 동시에 실행되는 연속조회 간에도 제한이
   * 공유된다. 이벤트루프 스레드를 막지 않도록 {@code Thread.sleep} 대신 {@link Mono#delay(Duration)} 로 대기한다.
   */
  private Mono<Void> acquirePermit() {
    return Mono.defer(
        () -> {
          long intervalNanos = kwProperties.rateLimit().minInterval().toNanos();
          long now = System.nanoTime();
          long reserved =
              nextPermitNanos.getAndUpdate(next -> Math.max(now, next) + intervalNanos);
          long waitNanos = reserved - now;
          return waitNanos > 0 ? Mono.delay(Duration.ofNanos(waitNanos)).then() : Mono.empty();
        });
  }

  /** 연속조회 헤더(cont-yn, next-key)를 주고받는 POST 요청. */
  private <T extends KwApiResponse> Mono<KwContinuousResponse<T>> postContinuous(
      String apiId,
      String uri,
      String accessToken,
      String nextKey,
      Object body,
      Class<T> responseType) {
    return kwWebClient
        .post()
        .uri(uri)
        .headers(
            headers -> {
              headers.setBearerAuth(accessToken);
              headers.set(HEADER_API_ID, apiId);
              if (nextKey != null && !nextKey.isBlank()) {
                headers.set(HEADER_CONT_YN, "Y");
                headers.set(HEADER_NEXT_KEY, nextKey);
              }
            })
        .bodyValue(body)
        .retrieve()
        .toEntity(responseType)
        .flatMap(entity -> toContinuousResponse(apiId, entity));
  }

  private static <T extends KwApiResponse> Mono<KwContinuousResponse<T>> toContinuousResponse(
      String apiId, ResponseEntity<T> entity) {
    HttpHeaders headers = entity.getHeaders();
    return validate(apiId, entity.getBody())
        .map(
            body ->
                new KwContinuousResponse<>(
                    body,
                    headers.getFirst(HEADER_CONT_YN),
                    headers.getFirst(HEADER_NEXT_KEY)));
  }

  /** 응답의 return_code 가 0 이 아니면 {@link KwApiException} 으로 종료한다. */
  private static <T extends KwApiResponse> Mono<T> validate(String apiId, T response) {
    if (response == null) {
      return Mono.error(new KwApiException(apiId, null, "empty response body"));
    }
    return response.isSuccess()
        ? Mono.just(response)
        : Mono.error(new KwApiException(apiId, response.returnCode(), response.returnMsg()));
  }
}
