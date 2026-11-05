package spendinglog.model;

import java.time.LocalDate;

/**
 * 가계부의 개별 수입/지출 내역 한 건을 표현하는 도메인 모델.
 *
 * <p>소비로그(Spending-Log)의 가장 기본이 되는 데이터 단위이며,
 * 함수 명세의 {@code addRecord(Record record)} 입력에 대응한다.
 * 하나의 내역은 날짜, 금액, 수입/지출 구분, 카테고리, 결제수단,
 * 해시태그, 메모로 구성된다.</p>
 *
 * @author 황시은 (255096)
 */
public class Record {

    /** 수입/지출을 구분하는 타입. */
    public enum Type {
        /** 지출(돈이 나감). */
        EXPENSE("지출"),
        /** 수입(돈이 들어옴). */
        INCOME("수입");

        private final String label;

        Type(String label) {
            this.label = label;
        }

        /** @return 화면에 표시할 한글 라벨 */
        public String getLabel() {
            return label;
        }
    }

    private LocalDate date;
    private long amount;
    private Type type;
    private String category;
    private String payment;
    private String hashtag;
    private String memo;

    /**
     * 모든 필드를 지정해 내역을 생성한다.
     *
     * @param date     내역 날짜
     * @param amount   금액(0 이상)
     * @param type     수입/지출 구분
     * @param category 카테고리(식비, 교통 등)
     * @param payment  결제수단(현금, 카드, 계좌)
     * @param hashtag  감정/상황 해시태그(예: #점심)
     * @param memo     자유 메모
     */
    public Record(LocalDate date, long amount, Type type,
                  String category, String payment, String hashtag, String memo) {
        this.date = date;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.payment = payment;
        this.hashtag = hashtag;
        this.memo = memo;
    }

    /** @return 내역 날짜 */
    public LocalDate getDate() {
        return date;
    }

    /** @param date 내역 날짜 */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /** @return 금액(0 이상) */
    public long getAmount() {
        return amount;
    }

    /** @param amount 금액(0 이상) */
    public void setAmount(long amount) {
        this.amount = amount;
    }

    /** @return 수입/지출 구분 */
    public Type getType() {
        return type;
    }

    /** @param type 수입/지출 구분 */
    public void setType(Type type) {
        this.type = type;
    }

    /** @return 카테고리 */
    public String getCategory() {
        return category;
    }

    /** @param category 카테고리 */
    public void setCategory(String category) {
        this.category = category;
    }

    /** @return 결제수단 */
    public String getPayment() {
        return payment;
    }

    /** @param payment 결제수단 */
    public void setPayment(String payment) {
        this.payment = payment;
    }

    /** @return 해시태그 */
    public String getHashtag() {
        return hashtag;
    }

    /** @param hashtag 해시태그 */
    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    /** @return 메모 */
    public String getMemo() {
        return memo;
    }

    /** @param memo 메모 */
    public void setMemo(String memo) {
        this.memo = memo;
    }

    /**
     * 지출 내역이면 금액에 음수 부호를, 수입이면 양수를 붙여 반환한다.
     * 테이블의 금액 열 표시에 사용된다.
     *
     * @return 부호가 적용된 금액
     */
    public long getSignedAmount() {
        return type == Type.EXPENSE ? -amount : amount;
    }

    @Override
    public String toString() {
        return String.format("%s %s %,d원(%s) %s",
                date, category, getSignedAmount(), payment,
                hashtag == null ? "" : hashtag);
    }
}
