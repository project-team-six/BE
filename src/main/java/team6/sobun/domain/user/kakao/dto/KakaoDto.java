package team6.sobun.domain.user.kakao.dto;

import lombok.Getter;

@Getter
public class KakaoDto {
    private Long id;
    private String email;
    private String username;

    public KakaoDto(Long id, String nickname, String email) {
        this.id = id;
        this.email = email;
        this.username = nickname;
    }
}