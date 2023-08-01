package team6.sobun.domain.user.dto.social;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FacebookUserInfo {
    private String id;
    private String name;
    private String email;
    private String picture;

    public FacebookUserInfo(String id, String name, String email, String picture) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.picture = picture;
    }
}
