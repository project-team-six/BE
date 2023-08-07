package team6.sobun.domain.post.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * team6.sobun.domain.post.dto.QPostResponseDto is a Querydsl Projection type for PostResponseDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QPostResponseDto extends ConstructorExpression<PostResponseDto> {

    private static final long serialVersionUID = 265924231L;

    public QPostResponseDto(com.querydsl.core.types.Expression<? extends team6.sobun.domain.post.entity.Post> post) {
        super(PostResponseDto.class, new Class<?>[]{team6.sobun.domain.post.entity.Post.class}, post);
    }

}

