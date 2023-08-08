package team6.sobun.domain.post.entity;

public enum PostStatus {
    IN_PROGRESS("게시글을 진행합니다."),
    COMPLETED("게시글을 마감했습니다.");

    private final String message;

    PostStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
