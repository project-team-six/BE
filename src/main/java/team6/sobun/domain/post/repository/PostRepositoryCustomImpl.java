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
import team6.sobun.domain.post.entity.Post;
import team6.sobun.domain.post.entity.QPost;
import team6.sobun.domain.user.entity.QUser;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;


@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public Page<PostResponseDto> serachPostByPage(PostSearchCondition condition, Pageable pageable) {
        List<PostResponseDto> result = query
                .select(new QPostResponseDto(
                        QPost.post.id,
                        QPost.post.category,
                        QPost.post.title,
                        QPost.post.user.nickname,
                        QPost.post.content,
                        QPost.post.createdAt,
                        QPost.post.imageUrlList,
                        QPost.post.pined,
                        QPost.post.views,
                        QPost.post.commentList.size().intValue(),
                        QPost.post.transactionStartDate,
                        QPost.post.transactionEndDate,
                        QPost.post.consumerPeriod,
                        QPost.post.purchaseDate,
                        QPost.post.location,
                        QPost.post.price
                ))
                .from(QPost.post)
                .where(
                        usernameEq(condition.getNickname()),
                        titleEq(condition.getTitle()))
                .orderBy(QPost.post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return new PageImpl<>(result, pageable, result.size());
    }


    /**
     * 사용자명에 대한 검색 조건을 생성하는 메소드입니다.
     *
     * @param usernameCond 사용자명 조건
     * @return 사용자명 조건에 해당하는 BooleanExpression 객체
     */
    private BooleanExpression usernameEq(String usernameCond) {
        return hasText(usernameCond) ? QPost.post.user.nickname.contains(usernameCond) : null;
    }

    /**
     * 제목에 대한 검색 조건을 생성하는 메소드입니다.
     *
     * @param titleCond 제목 조건
     * @return 제목 조건에 해당하는 BooleanExpression 객체
     */
    private BooleanExpression titleEq(String titleCond) {
        return hasText(titleCond) ? QPost.post.title.contains(titleCond) : null;
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
