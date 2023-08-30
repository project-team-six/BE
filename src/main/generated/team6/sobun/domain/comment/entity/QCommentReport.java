package team6.sobun.domain.comment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCommentReport is a Querydsl query type for CommentReport
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCommentReport extends EntityPathBase<CommentReport> {

    private static final long serialVersionUID = 545779869L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCommentReport commentReport = new QCommentReport("commentReport");

    public final team6.sobun.global.utils.QTimestamped _super = new team6.sobun.global.utils.QTimestamped(this);

    public final QComment comment;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<String, StringPath> imageUrlList = this.<String, StringPath>createList("imageUrlList", String.class, StringPath.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final EnumPath<CommentReportEnum> report = createEnum("report", CommentReportEnum.class);

    public final NumberPath<Long> reportedUserId = createNumber("reportedUserId", Long.class);

    public final team6.sobun.domain.user.entity.QUser reporter;

    public final StringPath type = createString("type");

    public QCommentReport(String variable) {
        this(CommentReport.class, forVariable(variable), INITS);
    }

    public QCommentReport(Path<? extends CommentReport> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCommentReport(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCommentReport(PathMetadata metadata, PathInits inits) {
        this(CommentReport.class, metadata, inits);
    }

    public QCommentReport(Class<? extends CommentReport> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.comment = inits.isInitialized("comment") ? new QComment(forProperty("comment"), inits.get("comment")) : null;
        this.reporter = inits.isInitialized("reporter") ? new team6.sobun.domain.user.entity.QUser(forProperty("reporter"), inits.get("reporter")) : null;
    }

}

