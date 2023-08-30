package team6.sobun.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team6.sobun.domain.post.entity.PostReport;

import java.util.List;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {

    List<PostReport> findByPost_User_Id(Long reportedUserId);
}
