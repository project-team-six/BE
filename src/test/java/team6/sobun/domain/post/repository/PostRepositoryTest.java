//package java.team6.sobun.domain.post.repository;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import team6.sobun.domain.post.dto.PostRequestDto;
//import team6.sobun.domain.post.entity.Post;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static team6.sobun.domain.post.entity.Category.FRESH_FOOD;
//
//@DataJpaTest
//class PostRepositoryTest {
//
//    @Test
//    @DisplayName("게시글 상세 조회")
//    void findDetailPost() {
//        // 1. given
//        String imageUrl = "https://gilgyu-bucket.s3.amazonaws.com/94898652-9d2e-4799-98c9-65471c63305e.jpg";
//        List<String> imageUrlList = Arrays.asList(imageUrl);
//        PostRequestDto postRequstDto = new PostRequestDto(FRESH_FOOD,
//                "프로필~!",
//                "흐음왜안될까요",
//                "2023-07-20",
//                "2023-08-06",
//                "2023-08-05",
//                "2023-08-01",
//                "30000",
//                "40000",
//                imageUrlList
//        );
//        Post post = new Post()
//        // 2. when
//        // 3. then
//    }
//
//    @Test
//    void findPostWithComments() {
//    }
//}