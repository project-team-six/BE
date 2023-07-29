package team6.sobun.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuerydslConfig {

    /**
     * JPAQueryFactory를 빈으로 생성하는 설정 클래스입니다.
     * QueryDSL을 사용하여 JPA 쿼리를 작성하는 데 사용됩니다.
     *
     * @param em EntityManager 인스턴스
     * @return JPAQueryFactory 인스턴스
     */
    @Bean
    JPAQueryFactory jpaQueryFactory(EntityManager em) {
        return new JPAQueryFactory(em);
    }
}
