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

<<<<<<< HEAD
    public QPostResponseDto(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<Long> userId,
                            com.querydsl.core.types.Expression<team6.sobun.domain.post.entity.Category> category,
                            com.querydsl.core.types.Expression<String> title,
                            com.querydsl.core.types.Expression<String> nickname,
                            com.querydsl.core.types.Expression<String> content,
                            com.querydsl.core.types.Expression<? extends java.util.List<team6.sobun.domain.comment.entity.Comment>> commentList,
                            com.querydsl.core.types.Expression<java.time.LocalDateTime> createdAt,
                            com.querydsl.core.types.Expression<String> location,
                            com.querydsl.core.types.Expression<String> price) {
        super(PostResponseDto.class, new Class<?>[]{long.class, long.class, team6.sobun.domain.post.entity.Category.class, String.class, String.class, String.class, java.util.List.class, java.time.LocalDateTime.class, String.class, String.class}, id, userId, category, title, nickname, content, commentList, createdAt, location, price);
=======
    public QPostResponseDto(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<Long> userId, com.querydsl.core.types.Expression<team6.sobun.domain.post.entity.Category> category, com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> nickname, com.querydsl.core.types.Expression<String> content, com.querydsl.core.types.Expression<java.time.LocalDateTime> createdAt, com.querydsl.core.types.Expression<String> location, com.querydsl.core.types.Expression<String> price) {
        super(PostResponseDto.class, new Class<?>[]{long.class, long.class, team6.sobun.domain.post.entity.Category.class, String.class, String.class, String.class, java.time.LocalDateTime.class, String.class, String.class}, id, userId, category, title, nickname, content, createdAt, location, price);
>>>>>>> d2f24a1482b952da7a8eb62426ecf0b8b290cb60
    }

}

