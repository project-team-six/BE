package team6.sobun.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.post.dto.PostResponseDto;
import team6.sobun.domain.post.entity.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
public class UserDetailResponseDto {

    private String nickname;
    private String profileImageUrl;
    private double mannerTemperature;
    private  String phoneNumber;
    private List<PostResponseDto> userPosts;
    private List<PostResponseDto> pinedPosts; //2

    public UserDetailResponseDto(String nickname, String profileImageUrl,String phoneNumber, double mannerTemperature, List<Post> userPosts, List<Post> pinedPosts) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.phoneNumber = phoneNumber;
        this.mannerTemperature = mannerTemperature;

        this.userPosts = userPosts.stream()
                .map(post -> new PostResponseDto(post.getId(), post.getCategory(), post.getTitle(), post.getUser().getNickname(),
                        post.getContent(), post.getCreatedAt(), post.getImageUrlList(), post.getPined(), post.getViews(), post.getCommentList().size(),
                        post.getTransactionStartDate(), post.getTransactionEndDate(), post.getConsumerPeriod(), post.getPurchaseDate(), post.getLocation(),
                        post.getPrice()))
                .collect(Collectors.toList());

        if (pinedPosts == null) {
            pinedPosts = new ArrayList<>();
        }

        // Post 엔티티 리스트를 PostResponseDto 리스트로 변환하여 설정합니다.
        this.pinedPosts = pinedPosts.stream()
                .map(post -> new PostResponseDto(post.getId(), post.getCategory(), post.getTitle(), post.getUser().getNickname(),
                        post.getContent(), post.getCreatedAt(), post.getImageUrlList(), post.getPined(), post.getViews(), post.getCommentList().size(),
                        post.getTransactionStartDate(), post.getTransactionEndDate(), post.getConsumerPeriod(), post.getPurchaseDate(), post.getLocation(),
                        post.getPrice()))
                .collect(Collectors.toList());
    }
}