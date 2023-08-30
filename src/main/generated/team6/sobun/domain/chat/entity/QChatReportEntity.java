package team6.sobun.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatReportEntity is a Querydsl query type for ChatReportEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatReportEntity extends EntityPathBase<ChatReportEntity> {

    private static final long serialVersionUID = 2116533962L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChatReportEntity chatReportEntity = new QChatReportEntity("chatReportEntity");

    public final team6.sobun.global.utils.QTimestamped _super = new team6.sobun.global.utils.QTimestamped(this);

    public final QChatMessageEntity chatMessageEntity;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<String, StringPath> imageUrlList = this.<String, StringPath>createList("imageUrlList", String.class, StringPath.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final EnumPath<team6.sobun.domain.chat.dto.ChatReportEnum> report = createEnum("report", team6.sobun.domain.chat.dto.ChatReportEnum.class);

    public final NumberPath<Long> reportedUserId = createNumber("reportedUserId", Long.class);

    public final team6.sobun.domain.user.entity.QUser reporter;

    public final StringPath type = createString("type");

    public QChatReportEntity(String variable) {
        this(ChatReportEntity.class, forVariable(variable), INITS);
    }

    public QChatReportEntity(Path<? extends ChatReportEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChatReportEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChatReportEntity(PathMetadata metadata, PathInits inits) {
        this(ChatReportEntity.class, metadata, inits);
    }

    public QChatReportEntity(Class<? extends ChatReportEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatMessageEntity = inits.isInitialized("chatMessageEntity") ? new QChatMessageEntity(forProperty("chatMessageEntity")) : null;
        this.reporter = inits.isInitialized("reporter") ? new team6.sobun.domain.user.entity.QUser(forProperty("reporter"), inits.get("reporter")) : null;
    }

}

