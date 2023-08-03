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
}

