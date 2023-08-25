package team6.sobun.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team6.sobun.domain.user.dto.UserResponseDto;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.entity.UserRoleEnum;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.global.responseDto.ApiResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPageService {

    public final UserRepository userRepository;

    public ApiResponse<?> userList() {
        List<User> userList = userRepository.findAll();
        List<UserResponseDto> userResponseDtoList = userList.stream().map(UserResponseDto::new).toList();

        return ApiResponse.success(userResponseDtoList);
    }

    public ApiResponse<?> userBlackList() {
        List<User> blackUserList = userRepository.findAllByBlackUser(UserRoleEnum.BLACK);
        List<UserResponseDto> userResponseDtoList = blackUserList.stream().map(UserResponseDto::new).toList();

        return ApiResponse.success(userResponseDtoList);
    }
}
