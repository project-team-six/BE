package team6.sobun.domain.user.dto.mypage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MypageRequestDto {

    @Pattern(regexp = "^[a-zA-Z0-9ㄱ-ㅎㅏ-ㅣ가-힣]*$", message = "닉네임은 한글, 영문 대소문자, 숫자만 입력하여야 합니다.")
    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    String nickname;

    @Pattern(regexp = "^(\\+?82|0)1[0-9]{1}[0-9]{3,4}[0-9]{4}$", message = "휴대전화 형식이 잘못 되었습니다.")
    @NotBlank(message = "전화번호는 필수 입력 값입니다.")
    String phoneNumber;
}
