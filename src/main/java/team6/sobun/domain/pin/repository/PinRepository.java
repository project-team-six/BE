package team6.sobun.domain.pin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team6.sobun.domain.pin.entity.Pin;
import team6.sobun.domain.post.entity.Post;
import team6.sobun.domain.user.entity.User;

import java.util.Optional;

public interface PinRepository extends JpaRepository<Pin, Long> {

    Optional<Pin> findByPostAndUser(Post post, User user);
}
