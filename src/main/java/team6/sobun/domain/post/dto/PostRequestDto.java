package team6.sobun.domain.post.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team6.sobun.domain.post.entity.Category;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostRequestDto {
    private Category category;
    @Pattern(regexp = ".{1,30}", message = "제목은 1자에서 30자까지만 허용됩니다.")
    private String title;
    @Pattern(regexp = ".{1,500}", message = "내용은 1자에서 500자까지만 허용됩니다.")
    private String content;
    private String transactionStartDate;
    private String transactionEndDate;
    private String consumerPeriod;
    private String purchaseDate;
    @Pattern(regexp = "\\d+", message = "가격은 숫자만 입력할 수 있습니다.")
    private String price;
    @Pattern(regexp = "\\d+", message = "가격은 숫자만 입력할 수 있습니다.")
    private String originPrice;
    private List<String> imageUrlList;

    public void setImageUrlList(List<String> imageUrlList) {
        this.imageUrlList = imageUrlList;
    }
}
