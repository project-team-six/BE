package team6.sobun.domain.comment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.comment.entity.CommentReportEnum;

@Getter
@NoArgsConstructor
public class CommentReportRequestDto {
    private CommentReportEnum commentReport;
}
