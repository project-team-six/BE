package team6.sobun.domain.chat.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginInfo {
    private String name;
    private String role;
    private String token;

    @Builder
    public LoginInfo(String name, String role,String token) {
        this.name = name;
        this.role = role;
        this.token = token;
    }
}
