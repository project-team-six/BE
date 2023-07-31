package team6.sobun.domain.pin.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPin is a Querydsl query type for Pin
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPin extends EntityPathBase<Pin> {

    private static final long serialVersionUID = 1063167049L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPin pin = new QPin("pin");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final team6.sobun.domain.post.entity.QPost post;

    public final team6.sobun.domain.user.entity.QUser user;

    public QPin(String variable) {
        this(Pin.class, forVariable(variable), INITS);
    }

    public QPin(Path<? extends Pin> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPin(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPin(PathMetadata metadata, PathInits inits) {
        this(Pin.class, metadata, inits);
    }

    public QPin(Class<? extends Pin> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.post = inits.isInitialized("post") ? new team6.sobun.domain.post.entity.QPost(forProperty("post"), inits.get("post")) : null;
        this.user = inits.isInitialized("user") ? new team6.sobun.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

