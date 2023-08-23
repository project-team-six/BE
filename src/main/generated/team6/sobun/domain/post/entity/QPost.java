package team6.sobun.domain.post.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPost is a Querydsl query type for Post
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPost extends EntityPathBase<Post> {

    private static final long serialVersionUID = 1601974147L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPost post = new QPost("post");

    public final team6.sobun.global.utils.QTimestamped _super = new team6.sobun.global.utils.QTimestamped(this);

    public final EnumPath<Category> category = createEnum("category", Category.class);

    public final ListPath<team6.sobun.domain.comment.entity.Comment, team6.sobun.domain.comment.entity.QComment> commentList = this.<team6.sobun.domain.comment.entity.Comment, team6.sobun.domain.comment.entity.QComment>createList("commentList", team6.sobun.domain.comment.entity.Comment.class, team6.sobun.domain.comment.entity.QComment.class, PathInits.DIRECT2);

    public final StringPath consumerPeriod = createString("consumerPeriod");

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<String, StringPath> imageUrlList = this.<String, StringPath>createList("imageUrlList", String.class, StringPath.class, PathInits.DIRECT2);

    public final StringPath location = createString("location");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath nickname = createString("nickname");

    public final StringPath originPrice = createString("originPrice");

    public final NumberPath<Long> pined = createNumber("pined", Long.class);

    public final NumberPath<Double> popularity = createNumber("popularity", Double.class);

    public final StringPath price = createString("price");

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final StringPath purchaseDate = createString("purchaseDate");

    public final EnumPath<PostStatus> status = createEnum("status", PostStatus.class);

    public final StringPath title = createString("title");

    public final StringPath transactionEndDate = createString("transactionEndDate");

    public final StringPath transactionStartDate = createString("transactionStartDate");

    public final team6.sobun.domain.user.entity.QUser user;

    public final NumberPath<Integer> views = createNumber("views", Integer.class);

    public QPost(String variable) {
        this(Post.class, forVariable(variable), INITS);
    }

    public QPost(Path<? extends Post> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPost(PathMetadata metadata, PathInits inits) {
        this(Post.class, metadata, inits);
    }

    public QPost(Class<? extends Post> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new team6.sobun.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

