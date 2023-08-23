package team6.sobun.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequestDto {

    @Email(message = "이메일 형식이 잘못되었거나 잘못된 이메일 입니다.")
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String email;

    @Pattern(regexp = "/^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{8,25}$/", message = "대소문자가 1개이상 포함하며 특수문자도 1개이상 포함한 8~25자 여야 합니다.")
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password;
}
