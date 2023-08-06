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

    public QPostResponseDto(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<team6.sobun.domain.post.entity.Category> category, com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> nickname, com.querydsl.core.types.Expression<String> content, com.querydsl.core.types.Expression<java.time.LocalDateTime> createdAt, com.querydsl.core.types.Expression<? extends java.util.List<String>> imageUrlList, com.querydsl.core.types.Expression<Long> pined, com.querydsl.core.types.Expression<Integer> views, com.querydsl.core.types.Expression<Integer> commentCount, com.querydsl.core.types.Expression<String> transactionStartDate, com.querydsl.core.types.Expression<String> transactionEndDate, com.querydsl.core.types.Expression<String> consumerPeriod, com.querydsl.core.types.Expression<String> purchaseDate, com.querydsl.core.types.Expression<String> location, com.querydsl.core.types.Expression<String> price) {
        super(PostResponseDto.class, new Class<?>[]{long.class, team6.sobun.domain.post.entity.Category.class, String.class, String.class, String.class, java.time.LocalDateTime.class, java.util.List.class, long.class, int.class, int.class, String.class, String.class, String.class, String.class, String.class, String.class}, id, category, title, nickname, content, createdAt, imageUrlList, pined, views, commentCount, transactionStartDate, transactionEndDate, consumerPeriod, purchaseDate, location, price);
    }

}

