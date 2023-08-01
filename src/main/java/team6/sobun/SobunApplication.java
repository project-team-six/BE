package team6.sobun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// 이미지 리사이징 라이브러리 사용 시 , 이미지IO와 변환 과정에서 3mb 이상 이미지는 리사이징하는 데 상당히 오랜 시간이 소요된다.
// 따라서 아래와 같이 multipart 사이즈에 제약 조건을 설정한다.
//spring.servlet.multipart.max-file-size=3MB
//spring.servlet.multipart.max-request-size=3MB

// yaml 형식
//        spring:
//        servlet:
//        multipart:
//        max-file-size: 3MB
//        max-request-size: 3MB

@EnableJpaAuditing
@SpringBootApplication
@EnableCaching
public class SobunApplication {

    public static void main(String[] args) {
        SpringApplication.run(SobunApplication.class, args);
    }

}
