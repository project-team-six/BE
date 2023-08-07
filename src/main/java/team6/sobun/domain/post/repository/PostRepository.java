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

    @Query("SELECT p FROM Post p WHERE p.user.id = :userId")
    List<Post> findPostsByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.pined WHERE p.id = :id")
    Post findByIdWithPins(@Param("id") Long id);

    @Query(value = "SELECT * FROM post WHERE title LIKE %?1% OR content LIKE %?1% ORDER BY createDate DESC" , nativeQuery = true)
    List<Post> findByContent(String content);

    @Query(value = "SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.commentList WHERE p.title LIKE %:searchKeyword% OR p.content LIKE %:searchKeyword% ORDER BY p.createdAt DESC")
    Slice<Post> findByTitleOrContentContainingWithComments(@Param("searchKeyword") String searchKeyword, Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.commentList c ORDER BY c.createdAt DESC")
    List<Post> findAllWithRecentComments();

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.commentList WHERE p.id = :postId")
    Post findPostWithComments(@Param("postId") Long postId);
}




