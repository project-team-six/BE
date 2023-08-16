package team6.sobun.domain.user.repository.mypage;
import org.springframework.data.jpa.repository.JpaRepository;
import team6.sobun.domain.user.entity.Mypage;
import team6.sobun.domain.user.entity.User;


public interface MypageRepository extends JpaRepository<Mypage, Long>,MypageRepositoryCustom {
}
