package team6.sobun.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserPopularity is a Querydsl query type for UserPopularity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserPopularity extends EntityPathBase<UserPopularity> {

    private static final long serialVersionUID = -1410320018L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserPopularity userPopularity = new QUserPopularity("userPopularity");

    public final QUser giver;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QUser receiver;

    public QUserPopularity(String variable) {
        this(UserPopularity.class, forVariable(variable), INITS);
    }

    public QUserPopularity(Path<? extends UserPopularity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserPopularity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserPopularity(PathMetadata metadata, PathInits inits) {
        this(UserPopularity.class, metadata, inits);
    }

    public QUserPopularity(Class<? extends UserPopularity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.giver = inits.isInitialized("giver") ? new QUser(forProperty("giver"), inits.get("giver")) : null;
        this.receiver = inits.isInitialized("receiver") ? new QUser(forProperty("receiver"), inits.get("receiver")) : null;
    }

}

