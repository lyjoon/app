# 프로젝트 개요
 - 이 프로젝트는 spring boot 4.1.1 기반 백엔드 서비스 프로젝트입니다.
 - 기술스택 : JAVA 17, Spring-Reactive, Maven, JPA, H2, MapStruct, Lombok
 
# 빌드 및 테스트 명령어
 - **빌드** mvn clean compile -DskipTests
 - **테스트** mvn test
 - **패키징** mvn clean package -DskipTests

# 아키텍처 및 패키지 구조
 * `src/main/java/org/llgs/global/`
   * `config/`: 전역 설정
   * `constant/`: 상수값 및 열거형 클래스
   * `exception/`: 예외클래스 정의
   * `util/`: 전역 유틸리티 정의
   * `dto/`: 전역 DTO 
   * `domain/`: 전역 JPA 도메인설정 
 * `src/test/java/`: 단위 및 통합 테스트 코드 위치
    
# 코딩 컨벤션 및 규칙
 * 자바 코드 스타일: Google Java Style Guide를 따름
 * 해당 모듈에서 테스트 코드는 지양합니다. 

# 해당 모듈 내 핵심업무용어
* **global** : 전역