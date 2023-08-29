package team6.sobun.domain.comment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.comment.entity.CommentReportEnum;

import java.util.List;

@Getter
@NoArgsConstructor
public class CommentReportRequestDto {
    private CommentReportEnum report;
    private List<String> imageUrlList;
}
