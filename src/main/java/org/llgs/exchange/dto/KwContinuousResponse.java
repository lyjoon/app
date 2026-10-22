package org.llgs.exchange.dto;

/**
 * 연속조회가 가능한 키움 REST-API 응답.
 *
 * <p>응답 헤더 {@code cont-yn}, {@code next-key} 를 응답 본문과 함께 담는다. {@link #hasNext()} 가 true 이면
 * {@link #nextKey()} 를 다음 요청에 전달하여 이어서 조회한다.
 *
 * @param body 응답 본문
 * @param contYn 연속조회여부 (Y/N)
 * @param nextKey 연속조회키
 */
public record KwContinuousResponse<T>(T body, String contYn, String nextKey) {

  public boolean hasNext() {
    return "Y".equals(contYn);
  }
}
