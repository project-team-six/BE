package team6.sobun.domain.post.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import team6.sobun.domain.post.entity.Post;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    @Query("select p from Post p left join fetch p.commentList cl join p.imageUrlList il where p.id = :postId")
    Optional<Post> findDetailPost(@Param("postId") Long postId);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.commentList WHERE p.id = :postId")
    Post findPostWithComments(@Param("postId") Long postId);

    Post findByChatroomId(String roomId);
}




