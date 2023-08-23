package team6.sobun.domain.post.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.springframework.format.annotation.DateTimeFormat;
import team6.sobun.domain.comment.entity.Comment;
import team6.sobun.domain.pin.entity.Pin;
import team6.sobun.domain.post.dto.PostRequestDto;
import team6.sobun.domain.user.entity.Location;
import team6.sobun.domain.user.entity.User;
import team6.sobun.global.utils.Timestamped;

import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;
import static org.hibernate.annotations.FetchMode.SUBSELECT;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Post extends Timestamped {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(nullable = false)
    private String title;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column
    private String location;

    @Column(nullable = false)
    private String price;

    @ElementCollection
    @BatchSize(size = 5)
    @Column
    private List<String> imageUrlList = new ArrayList<>(); // 이미지 URL 리스트

    //    @Temporal(TemporalType.DATE)
//    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private String transactionStartDate;

//    @Temporal(TemporalType.DATE)
//    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private String transactionEndDate;

//    @Temporal(TemporalType.DATE)
//    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column
    private String consumerPeriod;

//    @Temporal(TemporalType.DATE)
//    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column
    private String purchaseDate;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "post", cascade = ALL, orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private PostStatus status = PostStatus.IN_PROGRESS;

    @Column
    private double popularity;

    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = CASCADE)
    private User user;

    private int views ;

    private long pined ;

    private String profileImageUrl;

    @Column(nullable = false)
    private String originPrice;


    public Post(PostRequestDto postRequestDto, List<String> imageUrlList, User user) {
        this.category = postRequestDto.getCategory();
        this.title = postRequestDto.getTitle();
        this.content = postRequestDto.getContent();
        this.transactionStartDate = postRequestDto.getTransactionStartDate();
        this.transactionEndDate = postRequestDto.getTransactionEndDate();
        this.consumerPeriod = postRequestDto.getConsumerPeriod();
        this.purchaseDate = postRequestDto.getPurchaseDate();
        this.nickname = user.getNickname();
        this.imageUrlList = imageUrlList;
        this.location = user.getLocation().myAddress(user.getLocation().getSido(), user.getLocation().getSigungu(), user.getLocation().getDong());
        this.price = postRequestDto.getPrice();
        this.user = user;
        this.popularity = user.getPopularity();
        this.profileImageUrl = user.getProfileImageUrl();
        this.originPrice = postRequestDto.getOriginPrice();

    }
    public void markInProgress() {
        this.status = PostStatus.IN_PROGRESS;
    }
    public void markClosed() {
        this.status = PostStatus.COMPLETED;
    }

    public void update(PostRequestDto postRequestDto) {
        this.category = postRequestDto.getCategory();
        this.title = postRequestDto.getTitle();
        this.content = postRequestDto.getContent();
        this.transactionStartDate = postRequestDto.getTransactionStartDate();
        this.transactionEndDate = postRequestDto.getTransactionEndDate();
        this.consumerPeriod = postRequestDto.getConsumerPeriod();
        this.purchaseDate = postRequestDto.getPurchaseDate();
        this.price = postRequestDto.getPrice();
        this.originPrice = postRequestDto.getOriginPrice();
    }

    public void update(List<String> imageUrlList) {
        this.imageUrlList = imageUrlList;
    }


    public void increaseViews() {
        this.views++;
    }

    public void addComment(Comment comment) {
        commentList.add(comment);
        comment.initPost(this);
    }

    public void increasePin() {
        this.pined += 1;
    }

    public void decreasePin() {
        if (this.pined > 0) {
            this.pined -= 1;
        }
    }

    public void setImage(List<String> imageUrlList) {
        this.imageUrlList = imageUrlList;
    }
}
