package team6.sobun.domain.user.dto.social;

import lombok.Getter;
import lombok.Setter;
import team6.sobun.domain.user.entity.Location;

@Getter
@Setter
public class KakaoDto {
    private Long id;
    private String email;
    private Location location;
    private String phoneNumber;
    private String username;
    private String nickname;
    private String profileImageUrl;

    public KakaoDto(Long id, String email,String phoneNumber, String username, String nickname, String profileImageUrl) {
        this.id = id;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }
}