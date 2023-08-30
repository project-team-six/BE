package team6.sobun.domain.chat.dto;

public enum ChatReportEnum {
    PROFANITY("욕설"),
    DEFAMATION("비방"),
    HARASSMENT("성추행"),
    INCITING_DISPUTE("분쟁 유도"),
    POLITICAL_STATEMENT("정치성 발언"),
    OTHER("기타");

    private final String description;

    ChatReportEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
