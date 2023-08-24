package team6.sobun.domain.post.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.user.entity.User;
import team6.sobun.global.utils.Timestamped;

@Entity
@Getter
@NoArgsConstructor
public class PostReport extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @Enumerated(EnumType.STRING)
    private PostReportEnum report;

    public PostReport(User user, Post post, PostReportEnum report) {
        this.reporter = user;
        this.post = post;
        this.report = report;
    }
}
