package team6.sobun.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import team6.sobun.domain.post.entity.Post;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserDetailResponseDto {

    private final String nickname;
    private final String profileImageUrl;
    private final double mannerTemperature;
    private List<Post> userPosts;
    private List<Post> pinedPosts;

}
