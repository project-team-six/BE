package team6.sobun.domain.user.dto.find;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FindEmailRequestDto {

    @Pattern(regexp = "^[A-Za-z가-힣]+$", message = "이름은 영문 또는 한글만 가능합니다.")
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String username;

    @Pattern(regexp = "^(\\+?82|0)1[0-9]{1}[0-9]{3,4}[0-9]{4}$", message = "휴대전화 형식이 잘못 되었습니다.")
    @NotBlank(message = "전화번호는 필수 입력 값입니다.")
    private String phoneNumber;
}
