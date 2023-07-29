package team6.sobun.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.comment.dto.CommentResponseDto;
import team6.sobun.domain.post.entity.Category;
import team6.sobun.domain.post.entity.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponseDto {

    private Long id;
    private Category category;
    private String title;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
    private List<CommentResponseDto> commentList;
    private String image;
    private long liked;
    private Boolean isLike;
    private int views;
    private int commentCount;

    @QueryProjection
    public PostResponseDto(Long id, Category category, String title, String nickname, String content, LocalDateTime createdAt, String image, long liked, int views, int commentCount) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.nickname = nickname;
        this.content = content;
        this.createdAt = createdAt;
        this.commentCount = commentCount;
        this.image = image;
        this.liked = liked;
        this.views = views;
    }


    public PostResponseDto(Post post, Boolean isLike) {
        this.id = post.getId();
        this.category = post.getCategory();
        this.title = post.getTitle();
        this.nickname = post.getUser().getNickname();
        this.content = post.getContent();
        this.commentList = post.getCommentList().stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
        this.createdAt = post.getCreatedAt();
        this.image = post.getImage();
        this.liked = post.getLiked();
        this.views = post.getViews();
        this.isLike = isLike;
    }
}