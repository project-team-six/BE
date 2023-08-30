package team6.sobun.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team6.sobun.domain.comment.entity.CommentReportEnum;
import team6.sobun.domain.post.entity.PostReportEnum;
import team6.sobun.global.utils.Timestamped;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserReportResponseDto {

    private Long reportedUserId;
    private Long postId;
    private String postTitle;
    private String commentContent;
    private Long commentId;
    private Enum report;
    private String type;
    private List<String> reportImages;
    private Long postReportCount;
    private Long commentReportCount;
    private String email;
    private String profileImageUrl;
    private String nickname;
    private LocalDateTime createdAt;

    public UserReportResponseDto(Long reportedUserId, Long postReportCount,Long commentReportCount, String email, String profileImageUrl, String nickname) {
        this.reportedUserId = reportedUserId;
        this.postReportCount = postReportCount;
        this.commentReportCount = commentReportCount;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.nickname = nickname;
    }

    public UserReportResponseDto(Long reportedUserId, Long postId, Long commentId, String type, String commentContent, CommentReportEnum report, List<String> reportImages, LocalDateTime createdAt) {
        this.reportedUserId = reportedUserId;
        this.postId = postId;
        this.commentId = commentId;
        this.type = type;
        this.commentContent = commentContent;
        this.report = report;
        this.reportImages = reportImages;
        this.createdAt = createdAt;

    }

    public UserReportResponseDto(Long reportedUserId, Long postId, String type, String postTitle, PostReportEnum report, List<String> reportImages, LocalDateTime createdAt) {
        this.reportedUserId = reportedUserId;
        this.postId = postId;
        this.type = type;
        this.postTitle = postTitle;
        this.report = report;
        this.reportImages = reportImages;
        this.createdAt = createdAt;
    }

    public static List<UserReportResponseDto> removeDuplicateByPostId(List<UserReportResponseDto> reportList) {
        Set<Long> reportedIdSet = new HashSet<>();
        List<UserReportResponseDto> uniqueReports = new ArrayList<>();

        for (UserReportResponseDto report : reportList) {
            if (!reportedIdSet.contains(report.getReportedUserId())) {
                reportedIdSet.add(report.getReportedUserId());
                uniqueReports.add(report);
            }
        }

        return uniqueReports;
    }
}
