package team6.sobun.domain.user.repository.mypage;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import team6.sobun.domain.user.dto.mypage.MypageResponseDto;
import team6.sobun.domain.user.dto.mypage.MypageSearchCondition;
import team6.sobun.domain.user.dto.mypage.QMypageResponseDto;
import team6.sobun.domain.user.entity.QMypage;

import java.util.List;

@RequiredArgsConstructor
public class MypageRepositoryCustomImpl implements MypageRepositoryCustom {
    private final JPAQueryFactory query;

    @Override
    public Page<MypageResponseDto> searchMypageByPage(MypageSearchCondition condition, Pageable pageable) {
        List<MypageResponseDto> result = query
                .select (
                        new QMypageResponseDto(
                                QMypage.mypage.id,
                                QMypage.mypage.nickname,
                                QMypage.mypage.profileImageUrl,
                                QMypage.mypage.phoneNumber,
                                QMypage.mypage.mannerTemperature,
                                QMypage.mypage.userPosts,
                                QMypage.mypage.pinedPosts))
                .from(QMypage.mypage)
                .where(QMypage.mypage.id.eq(condition.getUserId())) // 사용자 ID 조건 추가
                .orderBy(QMypage.mypage.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long totalCount = query
                .select(QMypage.mypage.count())
                .from(QMypage.mypage)
                .where(QMypage.mypage.id.eq(condition.getUserId())) // 사용자 ID 조건 추가
                .fetchCount();

        return new PageImpl<>(result, pageable, totalCount);
    }
}