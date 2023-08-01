package team6.sobun.domain.post.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.OnDelete;
import org.springframework.format.annotation.DateTimeFormat;
import team6.sobun.domain.comment.entity.Comment;
import team6.sobun.domain.pin.entity.Pin;
import team6.sobun.domain.post.dto.PostRequestDto;
import team6.sobun.domain.user.entity.User;
import team6.sobun.global.utils.Timestamped;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
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

    private int views;

    @Column(nullable = false)
    private String title;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private Date transactionStartDate;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private Date transactionEndDate;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column
    private Date consumerPeriod;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column
    private Date PurchaseDate;


    @Fetch(SUBSELECT)
    @OneToMany(mappedBy = "post", cascade = ALL, orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();

    private long pined;

    private String image;

    @Enumerated(EnumType.STRING)
    private PostStatus status; // 게시물 상태 (진행중, 마감)

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = CASCADE)
    private User user;

    public Post(PostRequestDto postRequestDto, String image, User user) {
        this.category = postRequestDto.getCategory();
        this.title = postRequestDto.getTitle();
        this.transactionStartDate = postRequestDto.getTransactionStartDate();
        this.transactionEndDate = postRequestDto.getTransactionEndDate();
        this.consumerPeriod = postRequestDto.getConsumerPeriod();
        this.PurchaseDate = postRequestDto.getPurchaseDate();
        this.nickname = user.getNickname();
        this.content = postRequestDto.getContent();
        this.image = image;
        this.views = 0;
        this.pined = 0;
        this.user = user;

    }

    public void markInProgress() {
        this.status = PostStatus.IN_PROGRESS;
    }

    public void markClosed() {
        this.status = PostStatus.COMPLETED;
    }
    public void update(PostRequestDto postRequestDto) {
        this.title = postRequestDto.getTitle();
        this.content = postRequestDto.getContent();
    }

    public void updateAll(PostRequestDto postRequestDto, String image) {
        this.title = postRequestDto.getTitle();
        this.content = postRequestDto.getContent();
        this.image = image;
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

}
