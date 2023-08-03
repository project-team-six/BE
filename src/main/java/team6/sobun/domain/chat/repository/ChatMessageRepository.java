package team6.sobun.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team6.sobun.domain.chat.entity.ChatMessageEntity;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

}
