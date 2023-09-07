package team6.sobun.domain.post.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team6.sobun.domain.post.entity.Category;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostRequestDto {
    private Category category;
    @Pattern(regexp = ".{1,30}", message = "제목은 1자에서 30자까지만 허용됩니다.")
    private String title;
    @Pattern(regexp = "^[\\s\\S]{1,1500}$", message = "내용은 1자에서 1500자까지만 허용되며 줄바꿈도 허용됩니다.")
    private String content;
    private String transactionStartDate;
    private String transactionEndDate;
    private String consumerPeriod;
    private String purchaseDate;
    private String price;
    private String originPrice;
    private List<String> imageUrlList;

    public void setImageUrlList(List<String> imageUrlList) {
        this.imageUrlList = imageUrlList;
    }
}
