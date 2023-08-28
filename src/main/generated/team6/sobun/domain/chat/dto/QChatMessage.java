package team6.sobun.domain.chat.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * team6.sobun.domain.chat.dto.QChatMessage is a Querydsl Projection type for ChatMessage
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QChatMessage extends ConstructorExpression<ChatMessage> {

    private static final long serialVersionUID = 2064447024L;

    public QChatMessage(com.querydsl.core.types.Expression<? extends team6.sobun.domain.chat.entity.ChatMessageEntity> chatMessageEntity) {
        super(ChatMessage.class, new Class<?>[]{team6.sobun.domain.chat.entity.ChatMessageEntity.class}, chatMessageEntity);
    }

}

