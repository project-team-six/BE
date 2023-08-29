package team6.sobun.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import team6.sobun.domain.chat.entity.ChatRoomEntity;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, String> {

    List<ChatRoomEntity> findByNicknames(String userId);


}
