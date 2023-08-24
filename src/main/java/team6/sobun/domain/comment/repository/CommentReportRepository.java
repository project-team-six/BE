package team6.sobun.domain.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team6.sobun.domain.comment.entity.CommentReport;

public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {
}
