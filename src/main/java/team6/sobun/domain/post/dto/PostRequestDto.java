package team6.sobun.domain.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.post.entity.Category;

import java.util.Date;

@Getter
@NoArgsConstructor
public class PostRequestDto {
    private Category category;
    private String title;
    private String content;
    private Date transactionStartDate;
    private Date transactionEndDate;
    private Date consumerPeriod;
    private Date PurchaseDate;
}
