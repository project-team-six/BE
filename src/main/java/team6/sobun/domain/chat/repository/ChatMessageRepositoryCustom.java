package team6.sobun.domain.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.domain.chat.dto.ChatMessageSearchCondition;
import team6.sobun.domain.chat.entity.ChatMessageEntity;

public interface ChatMessageRepositoryCustom {

    Slice<ChatMessage> searchChatMessages(ChatMessageSearchCondition condition, Pageable pageable);
}
