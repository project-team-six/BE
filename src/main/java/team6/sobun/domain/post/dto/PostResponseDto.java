package team6.sobun.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.comment.dto.CommentResponseDto;
import team6.sobun.domain.comment.entity.Comment;
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
    private Long userId;
    private Category category;
    private String title;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
    private List<Comment> commentList;
    private List<String> imageUrlList;
    private String transactionStartDate;
    private String transactionEndDate;
    private String consumerPeriod;
    private String purchaseDate;
    private long pined;
    private Boolean isPin;
    private Boolean isComplete;
    private int views;
    private int size;
    private String location;
    private String price;

    @QueryProjection
    public PostResponseDto(Long id, Long userId, Category category, String title, String nickname, String content, List<Comment> commentList, LocalDateTime createdAt, String location, String price) {

        this.id = id;
        this.userId = userId;
        this.category = category;
        this.title = title;
        this.nickname = nickname;
        this.content = content;
        this.commentList = commentList;
        this.createdAt = createdAt;
        this.location = location;
        this.price = price;
    }


    public PostResponseDto(Post post,Boolean isComplete, Boolean isPin) {
        this.id = post.getId();
        this.userId = post.getUser().getId();
        this.category = post.getCategory();
        this.title = post.getTitle();
        this.nickname = post.getUser().getNickname();
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.imageUrlList = post.getImageUrlList().stream()
                .map(String::new)
                .collect(Collectors.toList());
        this.isComplete = isComplete;
        this.location = post.getLocation();
        this.price = post.getPrice();
    }

    public PostResponseDto(Long id, Long userId, Category category, String title, String nickname, String content, LocalDateTime createdAt, List<String> imageUrlList, long pined, int views, int size, String transactionStartDate, String transactionEndDate, String consumerPeriod, String purchaseDate, String location, String price) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.title = title;
        this.nickname = nickname;
        this.content = content;
        this.createdAt = createdAt;
        this.imageUrlList = imageUrlList;
        this.pined = pined;
        this.views = views;
        this.size = size;
        this.transactionStartDate = transactionStartDate;
        this.transactionEndDate = transactionEndDate;
        this.consumerPeriod = consumerPeriod;
        this.purchaseDate = purchaseDate;
        this.location = location;
        this.price = price;
    }
}

