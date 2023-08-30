package team6.sobun.domain.chat.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.domain.chat.dto.ChatMessageSearchCondition;
import team6.sobun.domain.chat.dto.QChatMessage;
import team6.sobun.domain.chat.entity.ChatMessageEntity;
import team6.sobun.domain.chat.entity.QChatMessageEntity;
import team6.sobun.domain.post.dto.PostResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class ChatMessageRepositoryCustomImpl implements ChatMessageRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public Slice<ChatMessage> searchChatMessages(ChatMessageSearchCondition condition, Pageable pageable) {
        int customPageSize = 50;
        pageable = PageRequest.of(pageable.getPageNumber(), customPageSize, pageable.getSort());
        List<ChatMessage> result = query
                .select(new QChatMessage(QChatMessageEntity.chatMessageEntity))
                .from(QChatMessageEntity.chatMessageEntity)
                .where(
                        roomIdEq(condition.getRoomId()),
                        createdAtGt(condition.getEntryTime()))
                .orderBy(QChatMessageEntity.chatMessageEntity.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return checkEndPage(pageable, result);
    }

    private BooleanExpression roomIdEq(String roomIdCond) {
        return hasText(roomIdCond) ? QChatMessageEntity.chatMessageEntity.roomId.eq(roomIdCond) : null;
    }

    private BooleanExpression createdAtGt(LocalDateTime entryTime) {
        return entryTime != null ? QChatMessageEntity.chatMessageEntity.createdAt.gt(entryTime) : null;
    }


    private static SliceImpl<ChatMessage> checkEndPage(Pageable pageable, List<ChatMessage> content) {
        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            hasNext = true;
            content.remove(pageable.getPageSize());
        }
        return new SliceImpl<>(content, pageable, hasNext);
    }
}
