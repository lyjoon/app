package org.llgs.global.config;

import org.llgs.global.constants.GlobalConstants;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 전역 JPA 자동 설정.
 *
 * <p>{@link GlobalConstants#GLOBAL_PACKAGE} 하위의 Entity / Repository 를 스캔하고, JPA Auditing 을 활성화한다.
 * global 모듈을 의존성으로 추가하면 {@code META-INF/spring/...AutoConfiguration.imports} 를 통해 자동 적용된다.
 */
@AutoConfiguration(
    before = {HibernateJpaAutoConfiguration.class, DataJpaRepositoriesAutoConfiguration.class})
@EnableTransactionManagement
@EnableJpaAuditing
@EntityScan(basePackages = GlobalConstants.GLOBAL_PACKAGE)
@EnableJpaRepositories(basePackages = GlobalConstants.GLOBAL_PACKAGE)
public class JpaConfig {}
