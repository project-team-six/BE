package team6.sobun.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatRoomEntity is a Querydsl query type for ChatRoomEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatRoomEntity extends EntityPathBase<ChatRoomEntity> {

    private static final long serialVersionUID = 40262129L;

    public static final QChatRoomEntity chatRoomEntity = new QChatRoomEntity("chatRoomEntity");

    public final team6.sobun.global.utils.QTimestamped _super = new team6.sobun.global.utils.QTimestamped(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final MapPath<String, java.time.LocalDateTime, DateTimePath<java.time.LocalDateTime>> entryTimes = this.<String, java.time.LocalDateTime, DateTimePath<java.time.LocalDateTime>>createMap("entryTimes", String.class, java.time.LocalDateTime.class, DateTimePath.class);

    public final StringPath lastMessage = createString("lastMessage");

    public final StringPath lastMessageSender = createString("lastMessageSender");

    public final StringPath lastMessageSenderProfileImageUrl = createString("lastMessageSenderProfileImageUrl");

    public final DateTimePath<java.time.LocalDateTime> lastMessageTime = createDateTime("lastMessageTime", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final ListPath<String, StringPath> nicknames = this.<String, StringPath>createList("nicknames", String.class, StringPath.class, PathInits.DIRECT2);

    public final StringPath roomId = createString("roomId");

    public final StringPath title = createString("title");

    public final StringPath titleImageUrl = createString("titleImageUrl");

    public QChatRoomEntity(String variable) {
        super(ChatRoomEntity.class, forVariable(variable));
    }

    public QChatRoomEntity(Path<? extends ChatRoomEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QChatRoomEntity(PathMetadata metadata) {
        super(ChatRoomEntity.class, metadata);
    }

}

