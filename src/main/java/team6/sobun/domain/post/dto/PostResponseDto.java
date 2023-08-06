package team6.sobun.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.comment.dto.CommentResponseDto;
import team6.sobun.domain.pin.entity.Pin;
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
    private List<CommentResponseDto> commentList;
    private List<String> imageUrlList;
    private String transactionStartDate;
    private String transactionEndDate;
    private String consumerPeriod;
    private String purchaseDate;
    private long pined;
    private Boolean isPin;
    private Boolean isComplete;
    private int views;
    private int commentCount;
    private String location;
    private String price;

    @QueryProjection
    public PostResponseDto(Long id, Category category, String title, String nickname, String content,
                           LocalDateTime createdAt, List<String> imageUrlList, long pined, int views,
                           int commentCount, String transactionStartDate, String transactionEndDate,
                           String consumerPeriod, String purchaseDate, String location, String price) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.nickname = nickname;
        this.content = content;
        this.createdAt = createdAt;
        this.commentCount = commentCount;
        this.imageUrlList = imageUrlList;
        this.pined = pined;
        this.views = views;
        this.transactionStartDate = transactionStartDate;
        this.transactionEndDate = transactionEndDate;
        this.consumerPeriod = consumerPeriod;
        this.purchaseDate = purchaseDate;
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
        this.commentList = post.getCommentList().stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
        this.createdAt = post.getCreatedAt();
        this.imageUrlList = post.getImageUrlList().stream()
                .map(String::new)
                .collect(Collectors.toList());
        this.transactionStartDate = post.getTransactionStartDate();
        this.transactionEndDate = post.getTransactionEndDate();
        this.consumerPeriod = post.getConsumerPeriod();
        this.purchaseDate = post.getPurchaseDate();
        this.pined = post.getPined();
        this.views = post.getViews();
        this.isPin = isPin;
        this.isComplete = isComplete;
        this.location = post.getLocation();
        this.price = post.getPrice();
    }

    public PostResponseDto(Post post, boolean pinedPost) {
    }
}