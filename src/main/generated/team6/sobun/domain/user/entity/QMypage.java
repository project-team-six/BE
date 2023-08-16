package team6.sobun.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMypage is a Querydsl query type for Mypage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMypage extends EntityPathBase<Mypage> {

    private static final long serialVersionUID = 2087391305L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMypage mypage = new QMypage("mypage");

    public final team6.sobun.global.utils.QTimestamped _super = new team6.sobun.global.utils.QTimestamped(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QLocation location;

    public final NumberPath<Double> mannerTemperature = createNumber("mannerTemperature", Double.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath nickname = createString("nickname");

    public final StringPath password = createString("password");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final ListPath<team6.sobun.domain.post.entity.Post, team6.sobun.domain.post.entity.QPost> pinedPosts = this.<team6.sobun.domain.post.entity.Post, team6.sobun.domain.post.entity.QPost>createList("pinedPosts", team6.sobun.domain.post.entity.Post.class, team6.sobun.domain.post.entity.QPost.class, PathInits.DIRECT2);

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final EnumPath<UserRoleEnum> role = createEnum("role", UserRoleEnum.class);

    public final QUser user;

    public final StringPath username = createString("username");

    public final ListPath<team6.sobun.domain.post.entity.Post, team6.sobun.domain.post.entity.QPost> userPosts = this.<team6.sobun.domain.post.entity.Post, team6.sobun.domain.post.entity.QPost>createList("userPosts", team6.sobun.domain.post.entity.Post.class, team6.sobun.domain.post.entity.QPost.class, PathInits.DIRECT2);

    public QMypage(String variable) {
        this(Mypage.class, forVariable(variable), INITS);
    }

    public QMypage(Path<? extends Mypage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMypage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMypage(PathMetadata metadata, PathInits inits) {
        this(Mypage.class, metadata, inits);
    }

    public QMypage(Class<? extends Mypage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.location = inits.isInitialized("location") ? new QLocation(forProperty("location"), inits.get("location")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}

