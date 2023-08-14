package team6.sobun.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team6.sobun.domain.chat.entity.ChatRoomEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, String> {

    Optional<ChatRoomEntity> findByUserIds(String userId);

}
