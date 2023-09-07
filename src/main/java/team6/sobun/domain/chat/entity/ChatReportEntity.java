package team6.sobun.domain.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import team6.sobun.domain.chat.dto.ChatReportEnum;
import team6.sobun.domain.user.entity.User;
import team6.sobun.global.utils.Timestamped;

import java.util.ArrayList;
import java.util.List;
@Entity
@Getter
@NoArgsConstructor
public class ChatReportEntity extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private ChatMessageEntity chatMessageEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @Column
    private Long reportedUserId;

    @Enumerated(EnumType.STRING)
    private ChatReportEnum report;

    @Column
    private String type = "CHAT";

    @ElementCollection
    @Fetch(FetchMode.JOIN)
    @BatchSize(size = 5)
    @Column
    private List<String> imageUrlList = new ArrayList<>();

    public ChatReportEntity(User user,Long reportedUserId, ChatMessageEntity chatMessageEntity,List<String> imageUrlList, ChatReportEnum report) {
        this.reporter = user;
        this.reportedUserId = reportedUserId;
        this.chatMessageEntity = chatMessageEntity;
        this.report = report;
        this.imageUrlList = imageUrlList;
    }
}
