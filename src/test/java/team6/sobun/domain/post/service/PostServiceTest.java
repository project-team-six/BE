//package team6.sobun.domain.post.service;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.transaction.annotation.Transactional;
//import team6.sobun.domain.chat.service.ChatService;
//import team6.sobun.domain.pin.repository.PinRepository;
//import team6.sobun.domain.post.dto.PostRequestDto;
//import team6.sobun.domain.post.entity.Post;
//import team6.sobun.domain.post.repository.PostReportRepository;
//import team6.sobun.domain.post.repository.PostRepository;
//import team6.sobun.domain.poststatus.repository.PostStatusRepository;
//import team6.sobun.domain.user.entity.User;
//import team6.sobun.domain.user.entity.UserRoleEnum;
//import team6.sobun.domain.user.repository.UserRepository;
//import team6.sobun.global.jwt.JwtProvider;
//import team6.sobun.global.responseDto.ApiResponse;
//import team6.sobun.global.stringCode.SuccessCodeEnum;
//import team6.sobun.global.utils.ResponseUtils;
//
//import java.util.Collections;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//@Transactional
//public class PostServiceTest {
//
//    @Autowired
//    private PostService postService;
//
//    @MockBean
//    private PostRepository postRepository;
//
//    @MockBean
//    private S3Service s3Service;
//
//    @MockBean
//    private ChatService chatService;
//
//    @MockBean
//    private JwtProvider jwtProvider;
//
//    @MockBean
//    private PinRepository pinRepository;
//
//    @MockBean
//    private PostStatusRepository postStatusRepository;
//
//    @MockBean
//    private UserRepository userRepository;
//
//    @MockBean
//    private PostReportRepository postReportRepository;
//
//    @Test
//    public void createPost_ValidData_Success() {
//        User user = User.builder()
//                .email("test@example.com")
//                .phoneNumber("1234567890")
//                .nickname("testuser")
//                .password("password123")
//                .username("testusername")
//                .role(UserRoleEnum.USER)
//                .build();
//
//        PostRequestDto postRequestDto = new PostRequestDto();
//        postRequestDto.setTitle("Test Post");
//        postRequestDto.setContent("This is a test post.");
//        postRequestDto.setImageUrlList(Collections.emptyList());
//
//        when(jwtProvider.getTokenFromHeader(any())).thenReturn("fakeToken");
//        when(jwtProvider.substringHeaderToken(any())).thenReturn("fakeToken");
//        when(jwtProvider.getUserInfoFromToken(any())).thenReturn(null);
//        when(userRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(user));
//
//        ApiResponse<?> response = postService.createPost(postRequestDto, Collections.emptyList(), user);
//
//        assertEquals(ResponseUtils.okWithMessage(SuccessCodeEnum.POST_CREATE_SUCCESS), response);
//        verify(postRepository, times(1)).save(any(Post.class));
//    }
//}