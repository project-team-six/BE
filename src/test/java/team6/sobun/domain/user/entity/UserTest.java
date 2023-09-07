//package team6.sobun.domain.user.entity;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.test.context.ActiveProfiles;
//import team6.sobun.domain.user.repository.UserRepository;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//class UserTest {
//
//    @DataJpaTest
//    @ActiveProfiles("test")
//    public class UserRepositoryTest {
//
//        @Autowired
//        private UserRepository userRepository;
//
//        @Test
//        public void saveUserAndRetrieveByEmail() {
//            // 새로운 User 엔티티 생성
//            User user = User.builder()
//                    .email("test@example.com")
//                    .phoneNumber("1234567890")
//                    .nickname("testuser")
//                    .password("password123")
//                    .username("testusername")
//                    .role(UserRoleEnum.USER)
//                    .build();
//
//            // UserRepository를 통해 User 엔티티 저장
//            userRepository.save(user);
//
//            // UserRepository를 통해 저장된 User 엔티티를 이메일로 조회
//            User savedUser = userRepository.findByEmail("test@example.com").orElse(null);
//
//            // 조회한 User 엔티티가 null이 아니어야 하고, 필드 값들이 일치하는지 검증
//            assertNotNull(savedUser);
//            assertEquals("test@example.com", savedUser.getEmail());
//            assertEquals("1234567890", savedUser.getPhoneNumber());
//            assertEquals("testuser", savedUser.getNickname());
//            assertEquals("password123", savedUser.getPassword());
//            assertEquals("testusername", savedUser.getUsername());
//            assertEquals(UserRoleEnum.USER, savedUser.getRole());
//        }
//    }
//}