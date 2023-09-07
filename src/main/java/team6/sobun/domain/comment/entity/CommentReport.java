package team6.sobun.domain.comment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import team6.sobun.domain.user.entity.User;
import team6.sobun.global.utils.Timestamped;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class CommentReport extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @Column
    private Long reportedUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Enumerated(EnumType.STRING)
    private CommentReportEnum report;

    @Column
    private String type = "COMMENT";

    @ElementCollection
    @Fetch(FetchMode.JOIN)
    @BatchSize(size = 5)
    @Column
    private List<String> imageUrlList = new ArrayList<>();

    public CommentReport(Comment comment, List<String> imageUrlList,CommentReportEnum report,Long reportedUserId, User user) {
        this.comment = comment;
        this.report =  report;
        this.reporter = user;
        this.reportedUserId = reportedUserId;
        this.imageUrlList = imageUrlList;
    }
}
