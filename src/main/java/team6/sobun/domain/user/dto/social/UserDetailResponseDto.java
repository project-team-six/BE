package team6.sobun.domain.user.dto.social;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDetailResponseDto {

    private final String nickname;
    private final String profileImageUrl;
    private final double mannerTemperature;
}
