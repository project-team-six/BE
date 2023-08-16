package team6.sobun.domain.user.repository.mypage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import team6.sobun.domain.user.dto.mypage.MypageResponseDto;
import team6.sobun.domain.user.dto.mypage.MypageSearchCondition;

public interface MypageRepositoryCustom {

    Page<MypageResponseDto> searchMypageByPage(MypageSearchCondition condition, Pageable pageable);
}
