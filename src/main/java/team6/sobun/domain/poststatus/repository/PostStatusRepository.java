package team6.sobun.domain.poststatus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team6.sobun.domain.post.entity.Post;
import team6.sobun.domain.poststatus.entity.PostStatus;
import team6.sobun.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface PostStatusRepository extends JpaRepository<PostStatus, Long> {

    Optional<PostStatus> findByPostAndUser(Post post, User user);
    List<Post> findByUser_Id(Long userId);
}
