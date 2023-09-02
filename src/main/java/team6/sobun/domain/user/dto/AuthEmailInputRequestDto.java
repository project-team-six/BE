package team6.sobun.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthEmailInputRequestDto {
    private String email;
    private String authNumber;
}
