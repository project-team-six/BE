package team6.sobun.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatRoomParticipant is a Querydsl query type for ChatRoomParticipant
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatRoomParticipant extends EntityPathBase<ChatRoomParticipant> {

    private static final long serialVersionUID = -290254363L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChatRoomParticipant chatRoomParticipant = new QChatRoomParticipant("chatRoomParticipant");

    public final QChatRoomEntity chatRoom;

    public final DateTimePath<java.time.LocalDateTime> entryTime = createDateTime("entryTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath nickname = createString("nickname");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QChatRoomParticipant(String variable) {
        this(ChatRoomParticipant.class, forVariable(variable), INITS);
    }

    public QChatRoomParticipant(Path<? extends ChatRoomParticipant> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChatRoomParticipant(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChatRoomParticipant(PathMetadata metadata, PathInits inits) {
        this(ChatRoomParticipant.class, metadata, inits);
    }

    public QChatRoomParticipant(Class<? extends ChatRoomParticipant> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatRoom = inits.isInitialized("chatRoom") ? new QChatRoomEntity(forProperty("chatRoom")) : null;
    }

}

