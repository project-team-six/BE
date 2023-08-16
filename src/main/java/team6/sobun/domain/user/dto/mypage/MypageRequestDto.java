package team6.sobun.domain.user.dto.mypage;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MypageRequestDto {
    String nickname;
    String password;
    String phoneNumber;
}
