package team6.sobun.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team6.sobun.domain.post.entity.PostReport;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {
}
