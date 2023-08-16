package team6.sobun.domain.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import team6.sobun.domain.user.dto.UserSearchCondition;
import team6.sobun.domain.user.dto.mypage.MypageResponseDto;

public interface UserPepositoryCustom {

    Page<MypageResponseDto> serachPostByPage(UserSearchCondition condition, Pageable pageable);
}
