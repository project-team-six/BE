package team6.sobun.domain.user.dto.password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordRequestDto {

    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{8,25}$", message = "비밀번호는 영문 대소문자 중 1개 이상, 특수문자 1개 이상을 포함한 8~25자 여야 합니다.")
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    String password;
}
