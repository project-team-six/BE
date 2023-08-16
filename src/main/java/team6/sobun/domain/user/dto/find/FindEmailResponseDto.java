package team6.sobun.domain.user.dto.find;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FindEmailResponseDto {
    private String email;

    public FindEmailResponseDto(String email) {
        this.email = email;
    }
}
