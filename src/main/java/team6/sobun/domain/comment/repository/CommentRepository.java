package team6.sobun.domain.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import team6.sobun.domain.comment.entity.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByOrderByIdDesc();

        List<Comment> findAllByPostIdOrderByCreatedAtDesc(Long postId);


    // 게시글 검색 기능 (댓글 내용에 특정 단어가 포함된 댓글 검색)
    @Query(value ="select * from comment where comment like %?1%", nativeQuery = true)
    List<Comment> findByCommentContaining(String keyword);
}
