package team6.sobun.global.stringCode;

import lombok.Getter;

@Getter
public enum SuccessCodeEnum {

    USER_SIGNUP_SUCCESS("회원가입 성공"),
    USER_LOGIN_SUCCESS("로그인 성공"),
    POST_CREATE_SUCCESS("게시글 작성 성공"),
    POST_UPDATE_SUCCESS("게시글 수정 성공"),
    POST_DELETE_SUCCESS("게시글 삭제 성공"),
    COMMENT_CREATE_SUCCESS("댓글 작성 완료"),
    COMMENT_UPDATE_SUCCESS("댓글 수정 완료"),
    COMMENT_DELETE_SUCCESS("댓글 삭제 성공"),
    LIKE_SUCCESS("좋아요 성공"),
    LIKE_CANCEL_SUCCESS("좋아요 취소"),
    USER_NICKNAME_SUCCESS("닉네임 변경 성공");


    private final String message;

    SuccessCodeEnum(String message) {
        this.message = message;
    }
}
