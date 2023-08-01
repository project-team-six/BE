package team6.sobun.domain.user.dto;

import lombok.Getter;

@Getter
public class KakaoDto {
    private Long id;
    private String email;
    private String nickname;
    private String profileImageUrl;

    public KakaoDto(Long id, String nickname, String email, String profileImageUrl) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }
}