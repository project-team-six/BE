package team6.sobun.domain.user.dto.social;

import lombok.Getter;

@Getter
public class KakaoDto {
    private Long id;
    private String email;
    private String location;
    private String phonenumber;
    private String username;
    private String nickname;
    private String profileImageUrl;

    public KakaoDto(Long id, String email, String location,String phonenumber, String username, String nickname, String profileImageUrl) {
        this.id = id;
        this.email = email;
        this.location = location;
        this.phonenumber = phonenumber;
        this.username = username;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }
}