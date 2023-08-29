package team6.sobun.domain.post.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import team6.sobun.domain.chat.service.ChatService;
import team6.sobun.domain.post.dto.PostRequestDto;
import team6.sobun.domain.post.entity.Category;
import team6.sobun.domain.post.entity.Post;
import team6.sobun.domain.post.repository.PostRepository;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.entity.UserRoleEnum;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.stringCode.SuccessCodeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.mockito.Mockito.*;

@SpringBootTest
class PostServiceTest {

    @Autowired
    private final PostService postService;

    @MockBean
    private PostRepository postRepository;

    @MockBean
    private S3Service s3Service;

    @MockBean
    private ChatService chatService;

    @MockBean
    private PostRequestDto postRequestDto;

    PostServiceTest(PostService postService) {
        this.postService = postService;
    }

    @BeforeEach
     void beforeEach() {
        User user =  User.builder()
                .email("test1234@gmail.com")
                .username("testtest")
                .nickname("test1234")
                .password("test1234!")
                .phoneNumber("01012345678")
                .profileImageUrl(null)
                .role(UserRoleEnum.USER)
                .build();

        PostRequestDto postRequestDto = new PostRequestDto();
        postRequestDto.setCategory(Category.ETC);
        postRequestDto.setTitle("title");
        postRequestDto.setContent("testContent");
        postRequestDto.setTransactionStartDate("2023-08-29");
        postRequestDto.setTransactionEndDate("2023-08-29");
        postRequestDto.setConsumerPeriod("2023-08-29");
        postRequestDto.setPurchaseDate("2023-08-29");
        postRequestDto.setPrice("30000");
        postRequestDto.setOriginPrice("30000");


    }
//    @Test
//    void createPostTest() {
//        List<String> imageUrlList = new ArrayList<>();
//        String chatRoomId = null;
//        // 목 데이터 준비
//        Post post = new Post(postRequestDto, imageUrlList, User.builder().build(), chatRoomId);
//        postRepository.save(post);
//
//        Post loadPost = postRepository.findById(post.getId()).orElseThrow();
//
//        MultipartFile multipartFile = new MockMultipartFile()
//
//        ApiResponse<?> response = postService.createPost(postRequestDto,null, User.builder().build());
//
//        // 단언문 수행
//        assertEquals(SuccessCodeEnum.POST_CREATE_SUCCESS.getMessage(), response.getMessage());
//        // 목 의존성과의 상호작용을 검증할 수도 있습니다
//        verify(s3Service, times(1)).uploads(images);
//        verify(postRepository, times(1)).save(any(Post.class));
//    }
}