package team6.sobun.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.comment.dto.CommentResponseDto;
import team6.sobun.domain.post.entity.Category;
import team6.sobun.domain.post.entity.Post;
import team6.sobun.domain.post.entity.PostStatus;

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
    private List<CommentResponseDto> commentList;
    private LocalDateTime createdAt;
    private List<String> imageUrlList;
    private String transactionStartDate;
    private String transactionEndDate;
    private String consumerPeriod;
    private String purchaseDate;
    private Boolean isComplete;
    private Boolean isPin;
    private long pined;
    private int views;
    private String location;
    private String price;
    private String status;

    // 전체 조회 시에 사용되는 생성자
    @QueryProjection
    public PostResponseDto(Post post) {
        this.id = post.getId();
        this.userId = post.getUser().getId();
        this.category = post.getCategory();
        this.title = post.getTitle();
        this.nickname = post.getUser().getNickname();
        this.imageUrlList = post.getImageUrlList().stream().limit(1)
                .map(String::new)
                .collect(Collectors.toList());
        this.createdAt = post.getCreatedAt();
        this.location = post.getLocation();
        this.views = post.getViews();
        this.pined = post.getPined();
        this.price = post.getPrice();
        if (post.getStatus() != null) {
            this.status = post.getStatus().name();
        }
    }


    // 상세 조회 시에 사용하는 생성자
    public PostResponseDto(Post post, Boolean isPin, Boolean isComplete) {
        this.id = post.getId();
        this.userId = post.getUser().getId();
        this.category = post.getCategory();
        this.pined = post.getPined();
        this.views = post.getViews();
        this.title = post.getTitle();
        this.nickname = post.getUser().getNickname();
        this.content = post.getContent();
        this.transactionStartDate = post.getTransactionStartDate();
        this.transactionEndDate = post.getTransactionEndDate();
        this.consumerPeriod = post.getConsumerPeriod();
        this.purchaseDate = post.getPurchaseDate();
        this.createdAt = post.getCreatedAt();
        this.imageUrlList = post.getImageUrlList().stream()
                .map(String::new)
                .collect(Collectors.toList());
        this.location = post.getLocation();
        this.price = post.getPrice();
        this.commentList = post.getCommentList().stream()
                .limit(3).map(CommentResponseDto::new)
                .collect(Collectors.toList());
        this.isPin = isPin;
        this.isComplete = isComplete;
        if (post.getStatus() != null) {
            this.status = post.getStatus().name();
        }
    }

    // 유저 조회 시에 사용하는 생성자

    public PostResponseDto(Long id, Long userId, Category category, String status, String title, String nickname, String content, LocalDateTime createdAt, List<String> imageUrlList, long pined, int views, String transactionStartDate, String transactionEndDate, String consumerPeriod, String purchaseDate, String location, String price) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.status = status;
        this.title = title;
        this.nickname = nickname;
        this.content = content;
        this.createdAt = createdAt;
        this.imageUrlList = imageUrlList;
        this.pined = pined;
        this.views = views;
        this.transactionStartDate = transactionStartDate;
        this.transactionEndDate = transactionEndDate;
        this.consumerPeriod = consumerPeriod;
        this.purchaseDate = purchaseDate;
        this.location = location;
        this.price = price;
        this.status = status;

    }
}

