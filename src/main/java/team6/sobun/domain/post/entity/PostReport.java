package team6.sobun.domain.post.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import team6.sobun.domain.user.entity.User;
import team6.sobun.global.utils.Timestamped;

import java.util.ArrayList;
import java.util.List;

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

    @Column
    private Long reportedUserId;

    @Enumerated(EnumType.STRING)
    private PostReportEnum report;

    @Column
    private String type = "POST";

    @ElementCollection(fetch = FetchType.EAGER)
    @BatchSize(size = 5)
    @Column
    private List<String> imageUrlList = new ArrayList<>();

    public PostReport(User user,Long reportedUserId, Post post,List<String> imageUrlList, PostReportEnum report) {
        this.reporter = user;
        this.reportedUserId = reportedUserId;
        this.post = post;
        this.report = report;
        this.imageUrlList = imageUrlList;
    }
}
