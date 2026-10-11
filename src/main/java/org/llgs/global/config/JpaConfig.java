package org.llgs.global.config;

import org.llgs.global.constant.GlobalConstant;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 전역 JPA 설정.
 *
 * <p>{@link GlobalConstant#GLOBAL_PACKAGE} 하위의 Entity / Repository 를 스캔하고, JPA Auditing 을 활성화한다.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
@EntityScan(basePackages = GlobalConstant.GLOBAL_PACKAGE)
@EnableJpaRepositories(basePackages = GlobalConstant.GLOBAL_PACKAGE)
public class JpaConfig {}
