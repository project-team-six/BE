package team6.sobun.domain.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthEmailRequestDto {

    @Email(message = "잘못된 이메일 형식입니다.")
    private String email;
}
