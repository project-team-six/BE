package team6.sobun.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordRequestDto {
    @Email(message = "이메일 형식으로 작성해주세요.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "이름 입력은 필수입니다.")
    private String username;

    @NotBlank(message = "전화번호 입력은 필수입니다.")
    private String phoneNumber;
}
