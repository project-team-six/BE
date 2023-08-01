// NaverUserInfo.java 파일

package team6.sobun.domain.user.dto.social;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NaverUserInfo {
    private String id;
    private String username;
    private String nickname;
    private String location;
    private String email;
    private String profileImageUrl;

    public NaverUserInfo(String id,String location, String username, String nickname, String email, String profileImageUrl) {
        this.id = id;
        this.location = location;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }
}
