package team6.sobun.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team6.sobun.domain.user.entity.Kakao;

// 기본적인 CRUD 함수를 가지고 있음
// JpaRepository를 상속했기 때문에 @Repository 어노테이션 불필요
public interface KakaoRepository extends JpaRepository<Kakao, Long> {

    // JPA findBy 규칙
    // select * from user_master where kakao_email = ?
    public Kakao findByKakaoEmail(String kakaoEmail);

    Kakao findByKakaoId(Long kakaoId);
}