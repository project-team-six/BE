package team6.sobun.domain.user.dto;

import lombok.Getter;
import team6.sobun.domain.post.entity.Post;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.entity.UserRoleEnum;

import java.util.List;

@Getter
public class UserResponseDto {
    private String email;
    private String username;
    private String profileImageUrl;
    private UserRoleEnum role;

    public UserResponseDto(User user) {
        this.email = user.getEmail();
        this.username = user.getNickname();
        this.profileImageUrl = user.getProfileImageUrl();
        this.role = user.getRole();
    }
}
