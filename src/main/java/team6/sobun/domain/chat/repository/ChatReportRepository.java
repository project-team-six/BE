package team6.sobun.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team6.sobun.domain.chat.entity.ChatReportEntity;

import java.util.List;

public interface ChatReportRepository extends JpaRepository<ChatReportEntity,Long> {
    List<ChatReportEntity> findByReportedUserId(Long reportedUserId);
}
