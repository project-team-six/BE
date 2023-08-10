package team6.sobun.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginInfo {
    private String username;
    private String userId;
    private String nickname;
    private String role;
    private String token;

    @Builder
    public LoginInfo(String username,String userId, String nickname, String role,String token) {
        this.username = username;
        this.userId = userId;
        this.nickname = nickname;
        this.role = role;
        this.token = token;
    }
}