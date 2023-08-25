package team6.sobun.domain.post.entity;

public enum PostReportEnum {
    SOBUN_PROHIBITED("소분 금지 물품이예요"),
    NON_TRADE_POST("거래관련 게시글이 아니예요"),
    FRAUD("사기 글이예요"),
    DISPUTE_INDUCEMENT("분쟁 유도 글이예요"),
    ADVERTISEMENT("광고성 게시글이예요"),
    OTHER("기타");

    private final String description;

    PostReportEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}