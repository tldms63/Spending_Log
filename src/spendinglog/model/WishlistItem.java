package spendinglog.model;

/**
 * 위시리스트 저축 항목을 표현하는 도메인 모델.
 *
 * <p>사용자가 사고 싶은 물건의 이름과 목표 가격을 등록하면 하나의
 * {@code WishlistItem}이 생성되고, 저축액이 쌓일수록 달성률이 올라간다.
 * 함수 명세의 {@code updateSaving(String itemId, int amount)}가
 * 이 객체의 {@code saved} 값을 갱신한다.</p>
 *
 * @author 황시은 (255096)
 */
public class WishlistItem {

    private String name;
    private long targetPrice;
    private long saved;

    /**
     * 위시리스트 항목을 생성한다. 초기 저축액은 0이다.
     *
     * @param name        물건 이름(예: 무선 이어폰)
     * @param targetPrice 목표 가격(1 이상)
     */
    public WishlistItem(String name, long targetPrice) {
        this.name = name;
        this.targetPrice = targetPrice;
        this.saved = 0;
    }

    /** @return 물건 이름 */
    public String getName() {
        return name;
    }

    /** @param name 물건 이름 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 목표 가격 */
    public long getTargetPrice() {
        return targetPrice;
    }

    /** @param targetPrice 목표 가격(1 이상) */
    public void setTargetPrice(long targetPrice) {
        this.targetPrice = targetPrice;
    }

    /** @return 지금까지 배정한 저축액 */
    public long getSaved() {
        return saved;
    }

    /** @param saved 저축액 */
    public void setSaved(long saved) {
        this.saved = saved;
    }

    /**
     * 저축액을 더한다. 음수는 0으로 막아 저축액이 음수가 되지 않게 한다.
     *
     * @param amount 추가 저축액
     */
    public void addSaving(long amount) {
        this.saved += amount;
        if (this.saved < 0) {
            this.saved = 0;
        }
    }

    /**
     * 목표가 대비 저축 달성률을 0.0~1.0 범위로 반환한다.
     *
     * @return 달성률(목표가가 0 이하이면 0.0)
     */
    public double getProgress() {
        if (targetPrice <= 0) {
            return 0.0;
        }
        double p = (double) saved / targetPrice;
        return p > 1.0 ? 1.0 : p;
    }

    /** @return 달성률이 100%에 도달했는지 여부 */
    public boolean isAchieved() {
        return saved >= targetPrice;
    }
}
