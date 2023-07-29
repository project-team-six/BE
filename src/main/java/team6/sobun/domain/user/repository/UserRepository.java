package team6.sobun.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import team6.sobun.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByKakaoId(Long kakaoId);

    Optional<User> findByNickname(String nickname);

    @Query(value = "select UserName from user", nativeQuery = true)
    User findUserName();

    @Query(value =" select * from user where email like %?1%", nativeQuery = true)
    List<User> mFindByEmail(String email);
}
