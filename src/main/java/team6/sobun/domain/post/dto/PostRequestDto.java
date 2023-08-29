package team6.sobun.domain.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team6.sobun.domain.post.entity.Category;

import java.util.Date;
import java.util.List;

@Getter
@NoArgsConstructor
public class PostRequestDto {
    private Category category;
    private String title;
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
