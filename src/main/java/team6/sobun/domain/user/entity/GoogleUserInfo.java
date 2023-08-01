package team6.sobun.domain.user.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoogleUserInfo {
    private String sub;
    private String name;
    private String given_name;
    private String picture;
    private String email;
    private boolean email_verified;
    private String locale;
}
