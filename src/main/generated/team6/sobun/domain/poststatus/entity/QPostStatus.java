package team6.sobun.domain.poststatus.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPostStatus is a Querydsl query type for PostStatus
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPostStatus extends EntityPathBase<PostStatus> {

    private static final long serialVersionUID = -167585721L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPostStatus postStatus = new QPostStatus("postStatus");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final team6.sobun.domain.post.entity.QPost post;

    public final EnumPath<team6.sobun.domain.post.entity.PostStatus> status = createEnum("status", team6.sobun.domain.post.entity.PostStatus.class);

    public final team6.sobun.domain.user.entity.QUser user;

    public QPostStatus(String variable) {
        this(PostStatus.class, forVariable(variable), INITS);
    }

    public QPostStatus(Path<? extends PostStatus> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPostStatus(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPostStatus(PathMetadata metadata, PathInits inits) {
        this(PostStatus.class, metadata, inits);
    }

    public QPostStatus(Class<? extends PostStatus> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.post = inits.isInitialized("post") ? new team6.sobun.domain.post.entity.QPost(forProperty("post"), inits.get("post")) : null;
        this.user = inits.isInitialized("user") ? new team6.sobun.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

