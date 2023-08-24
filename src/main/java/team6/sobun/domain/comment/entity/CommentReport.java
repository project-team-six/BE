package team6.sobun.domain.comment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.user.entity.User;

@Entity
@Getter
@NoArgsConstructor
public class CommentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Enumerated(EnumType.STRING)
    private CommentReportEnum report;

    public CommentReport(Comment comment, CommentReportEnum commentReport, User user) {
        this.comment = comment;
        this.report = commentReport;
        this.reporter = user;
    }
}
