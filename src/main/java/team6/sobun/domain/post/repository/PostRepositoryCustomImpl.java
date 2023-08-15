package team6.sobun.domain.post.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
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
                        contentEq(condition.getTitle()),
                        categoryEq(condition.getCategory()),
                        locationEq(condition.getLocation()),
                        statusEq(condition.getStatus()))
                .orderBy(QPost.post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();


        return new PageImpl<>(result, pageable, result.size());
    }


    private BooleanExpression usernameEq(String usernameCond) {
        return hasText(usernameCond) ? QPost.post.user.nickname.contains(usernameCond) : null;
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






    /**
     * 페이징된 게시물 목록을 반환하고, 다음 페이지 여부를 확인하는 메소드입니다.
     *
     * @param pageable 페이징 정보
     * @param content  조회된 게시물 목록
     * @return 페이징된 게시물 목록
     */
    private static SliceImpl<PostResponseDto> checkEndPage(Pageable pageable, List<PostResponseDto> content) {
        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            hasNext = true;
            content.remove(pageable.getPageSize());
        }
        return new SliceImpl<>(content, pageable, hasNext);
    } // 무한 스크롤 사용시
}
