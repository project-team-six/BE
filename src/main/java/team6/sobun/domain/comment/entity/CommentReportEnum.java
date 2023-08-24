package team6.sobun.domain.comment.entity;

public enum CommentReportEnum {
    ABUSIVE_COMMENT("욕설 댓글이예요"),
    FRAUD("사기 글이예요"),
    DISPUTE_INDUCEMENT("분쟁 유도 댓글이예요"),
    ADVERTISEMENT("광고성 댓글이예요"),
    OTHER("기타");

    private final String description;

    CommentReportEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}