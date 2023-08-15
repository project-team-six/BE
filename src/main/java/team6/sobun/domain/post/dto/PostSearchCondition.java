package team6.sobun.domain.post.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostSearchCondition {

    private String nickname;
    private String title;
    private String content;
    private String category;
    private String location;
    private String status;
}
