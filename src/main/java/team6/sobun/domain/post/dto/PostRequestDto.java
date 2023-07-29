package team6.sobun.domain.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.post.entity.Category;

@Getter
@NoArgsConstructor
public class PostRequestDto {
    private Category category;
    private String title;
    private String content;
}
