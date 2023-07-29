package team6.sobun.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QKakao is a Querydsl query type for Kakao
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QKakao extends EntityPathBase<Kakao> {

    private static final long serialVersionUID = -73778955L;

    public static final QKakao kakao = new QKakao("kakao");

    public final DateTimePath<java.sql.Timestamp> createTime = createDateTime("createTime", java.sql.Timestamp.class);

    public final StringPath kakaoEmail = createString("kakaoEmail");

    public final NumberPath<Long> kakaoId = createNumber("kakaoId", Long.class);

    public final StringPath kakaoNickname = createString("kakaoNickname");

    public final StringPath kakaoProfileImg = createString("kakaoProfileImg");

    public final NumberPath<Long> userCode = createNumber("userCode", Long.class);

    public final StringPath userRole = createString("userRole");

    public QKakao(String variable) {
        super(Kakao.class, forVariable(variable));
    }

    public QKakao(Path<? extends Kakao> path) {
        super(path.getType(), path.getMetadata());
    }

    public QKakao(PathMetadata metadata) {
        super(Kakao.class, metadata);
    }

}

