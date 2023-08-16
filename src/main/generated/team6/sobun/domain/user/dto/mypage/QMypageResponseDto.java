package team6.sobun.domain.user.dto.mypage;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * team6.sobun.domain.user.dto.mypage.QMypageResponseDto is a Querydsl Projection type for MypageResponseDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QMypageResponseDto extends ConstructorExpression<MypageResponseDto> {

    private static final long serialVersionUID = -1182631716L;

    public QMypageResponseDto(com.querydsl.core.types.Expression<Long> userId, com.querydsl.core.types.Expression<String> nickname, com.querydsl.core.types.Expression<String> profileImageUrl, com.querydsl.core.types.Expression<String> phoneNumber, com.querydsl.core.types.Expression<Double> mannerTemperature, com.querydsl.core.types.Expression<? extends java.util.List<team6.sobun.domain.post.entity.Post>> userPosts, com.querydsl.core.types.Expression<? extends java.util.List<team6.sobun.domain.post.entity.Post>> pinedPosts) {
        super(MypageResponseDto.class, new Class<?>[]{long.class, String.class, String.class, String.class, double.class, java.util.List.class, java.util.List.class}, userId, nickname, profileImageUrl, phoneNumber, mannerTemperature, userPosts, pinedPosts);
    }

}

