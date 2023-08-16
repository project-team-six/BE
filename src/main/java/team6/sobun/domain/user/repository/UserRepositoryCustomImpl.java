//package team6.sobun.domain.user.repository;
//
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import team6.sobun.domain.user.dto.UserSearchCondition;
//import team6.sobun.domain.user.dto.mypage.MypageResponseDto;
//import team6.sobun.domain.user.dto.mypage.QMypageResponseDto;
//import team6.sobun.domain.user.entity.QUser;
//
//import java.util.List;
//
//@RequiredArgsConstructor
//public class UserRepositoryCustomImpl implements UserPepositoryCustom{
//    private final JPAQueryFactory query;
//
//    @Override
//    public Page<MypageResponseDto> serachPostByPage(UserSearchCondition condition, Pageable pageable) {
//
//        List<MypageResponseDto> result = query
//                .select(new QMypageResponseDto(QUser.user))
//                .from(QUser.user)
//                .orderBy(QUser.user.createdAt.desc())
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();
//
//        return new PageImpl<>( result, pageable, result.size());
//    }
//}
