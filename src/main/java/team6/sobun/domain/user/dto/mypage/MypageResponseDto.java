package team6.sobun.domain.user.dto.mypage;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.post.dto.PostResponseDto;
import team6.sobun.domain.post.entity.Post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
public class MypageResponseDto {

    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private double popularity;
    private String phoneNumber;
    private Boolean emailOk;
    private List<PostResponseDto> userPosts;
    private List<PostResponseDto> pinedPosts;



    public MypageResponseDto(Long userId, String nickname, String profileImageUrl, String phoneNumber,Boolean emailOk, double popularity, List<Post> userPosts, List<Post> pinedPosts) {
        this.userId = userId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.phoneNumber = phoneNumber;
        this.emailOk = emailOk;
        this.popularity = popularity;

        // 사용자의 게시물 리스트를 PostResponseDto 리스트로 변환하여 설정합니다.
        this.userPosts = userPosts.stream()
                .map(post -> new PostResponseDto(post.getId(), post.getUser().getId(), post.getCategory(),post.getStatus().name(), post.getTitle(), post.getUser().getNickname(),
                        post.getContent(), post.getCreatedAt(), Collections.singletonList(post.getImageUrlList().get(0)), post.getPined(), post.getViews(),
                        post.getTransactionStartDate(), post.getTransactionEndDate(), post.getConsumerPeriod(), post.getPurchaseDate(), post.getLocation(),
                        post.getPrice(), post.getOriginPrice()))
                .collect(Collectors.toList());

        if (pinedPosts == null) {
            pinedPosts = new ArrayList<>();
        }

        // 핀한 게시물 리스트를 PostResponseDto 리스트로 변환하여 설정합니다.
        this.pinedPosts = pinedPosts.stream()
                .map(post -> new PostResponseDto(post.getId(), post.getUser().getId(), post.getCategory(),post.getStatus().name(), post.getTitle(), post.getUser().getNickname(),
                        post.getContent(), post.getCreatedAt(), Collections.singletonList(post.getImageUrlList().get(0)), post.getPined(), post.getViews(),
                        post.getTransactionStartDate(), post.getTransactionEndDate(), post.getConsumerPeriod(), post.getPurchaseDate(), post.getLocation(),
                        post.getPrice(), post.getOriginPrice()))
                .collect(Collectors.toList());
    }
}
