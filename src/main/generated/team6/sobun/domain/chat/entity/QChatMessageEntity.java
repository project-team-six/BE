package team6.sobun.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatMessageEntity is a Querydsl query type for ChatMessageEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatMessageEntity extends EntityPathBase<ChatMessageEntity> {

    private static final long serialVersionUID = 1704669335L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChatMessageEntity chatMessageEntity = new QChatMessageEntity("chatMessageEntity");

    public final QChatRoomEntity chatRoom;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath message = createString("message");

    public final StringPath sender = createString("sender");

    public final StringPath type = createString("type");

    public QChatMessageEntity(String variable) {
        this(ChatMessageEntity.class, forVariable(variable), INITS);
    }

    public QChatMessageEntity(Path<? extends ChatMessageEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChatMessageEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChatMessageEntity(PathMetadata metadata, PathInits inits) {
        this(ChatMessageEntity.class, metadata, inits);
    }

    public QChatMessageEntity(Class<? extends ChatMessageEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatRoom = inits.isInitialized("chatRoom") ? new QChatRoomEntity(forProperty("chatRoom")) : null;
    }

}

