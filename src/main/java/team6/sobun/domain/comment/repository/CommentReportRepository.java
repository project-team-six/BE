package team6.sobun.domain.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {

    List<CommentReport> findByComment_User_Id(Long reportedUserId);
}
