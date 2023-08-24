package team6.sobun.domain.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.post.entity.PostReportEnum;

@Getter
@NoArgsConstructor
public class PostReportRequestDto {
    private PostReportEnum report;
}
