package team6.sobun.domain.poststatus.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import team6.sobun.domain.post.entity.Post;
import team6.sobun.domain.user.entity.User;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "post_statuses")
public class PostStatus {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private team6.sobun.domain.post.entity.PostStatus status; // PostStatusEnum은 IN_PROGRESS와 COMPLETED를 가지는 enum 타입

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id")
    @OnDelete(action = CASCADE)
    private Post post;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public PostStatus(Post post, User user) {
        this.post = post;
        this.user = user;
    }
    public void markInProgress() {
        this.status = team6.sobun.domain.post.entity.PostStatus.IN_PROGRESS;
    }

    public void markCompleted() {
        this.status = team6.sobun.domain.post.entity.PostStatus.COMPLETED;
    }
}


