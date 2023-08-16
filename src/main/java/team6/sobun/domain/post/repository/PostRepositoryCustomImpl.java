package team6.sobun.domain.post.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import team6.sobun.domain.post.dto.PostResponseDto;
import team6.sobun.domain.post.dto.PostSearchCondition;
import team6.sobun.domain.post.dto.QPostResponseDto;
import team6.sobun.domain.post.entity.Category;
import team6.sobun.domain.post.entity.PostStatus;
import team6.sobun.domain.post.entity.QPost;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;


@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public Page<PostResponseDto> serachPostByPage(PostSearchCondition condition, Pageable pageable) {
        List<PostResponseDto> result = query
                .select(new QPostResponseDto(QPost.post))
                .from(QPost.post)
                .where(
                        usernameEq(condition.getNickname()),
                        titleEq(condition.getTitle()),
                        contentEq(condition.getContent()),
                        titleOrContentEq(condition.getTitleOrContent()),
                        categoryEq(condition.getCategory()),
                        locationEq(condition.getLocation()),
                        statusEq(condition.getStatus()))
                .orderBy(QPost.post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        long totalCount = query
                .select(QPost.post.count())
                .from(QPost.post)
                .where(
                        usernameEq(condition.getNickname()),
                        titleEq(condition.getTitle()),
                        contentEq(condition.getContent()),
                        titleOrContentEq(condition.getTitleOrContent()),
                        categoryEq(condition.getCategory()),
                        locationEq(condition.getLocation()),
                        statusEq(condition.getStatus()))
                .fetchCount();


        return new PageImpl<>(result, pageable, totalCount);
    }


    private BooleanExpression usernameEq(String usernameCond) {
        return hasText(usernameCond) ? QPost.post.user.nickname.contains(usernameCond) : null;
    }
    private BooleanExpression titleOrContentEq(String titleOrContent) {
        return hasText(titleOrContent) ?
                QPost.post.title.contains(titleOrContent)
                        .or(QPost.post.content.contains(titleOrContent)) : null;
    }

    private BooleanExpression titleEq(String titleCond) {
        return hasText(titleCond) ? QPost.post.title.contains(titleCond) : null;
    }
    private BooleanExpression contentEq(String contentCond) {
        return hasText(contentCond) ? QPost.post.content.contains(contentCond) : null;
    }
    private BooleanExpression locationEq(String locationCond) {
        return hasText(locationCond) ? QPost.post.location.contains(locationCond) : null;
    }

    private BooleanExpression categoryEq(String categoryCond) {
        return hasText(categoryCond) ? QPost.post.category.eq(Category.valueOf(categoryCond)) : null;
    }

    private BooleanExpression statusEq(String statusCond) {
        return hasText(statusCond) ? QPost.post.status.eq(PostStatus.valueOf(statusCond)) : null;
    }
}
