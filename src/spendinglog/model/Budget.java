package spendinglog.model;

/**
 * 예산 설정 정보를 담는 도메인 모델.
 *
 * <p>월 총예산, 짠돌이 모드의 경고 한도(권장 지출), 짠돌이 모드 on/off,
 * 치킨 1마리 기준 단가를 보관한다. {@code BudgetManager}가 이 값을 읽어
 * 예산 초과 여부를 판단하고, {@code toChickenIndex} 계산의 단가로 쓴다.</p>
 *
 * @author 황시은 (255096)
 */
public class Budget {

    private long monthlyBudget;
    private long warningLimit;
    private boolean stingyMode;
    private int chickenUnitPrice;

    /**
     * 기본값으로 예산을 생성한다.
     * 월 예산 300,000원, 경고 한도 200,000원, 짠돌이 모드 off,
     * 치킨 단가 20,000원.
     */
    public Budget() {
        this.monthlyBudget = 300_000L;
        this.warningLimit = 200_000L;
        this.stingyMode = false;
        this.chickenUnitPrice = 20_000;
    }

    /** @return 월 총예산 */
    public long getMonthlyBudget() {
        return monthlyBudget;
    }

    /** @param monthlyBudget 월 총예산 */
    public void setMonthlyBudget(long monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    /** @return 짠돌이 모드 경고 한도(권장 지출) */
    public long getWarningLimit() {
        return warningLimit;
    }

    /** @param warningLimit 경고 한도 */
    public void setWarningLimit(long warningLimit) {
        this.warningLimit = warningLimit;
    }

    /** @return 짠돌이 모드 활성화 여부 */
    public boolean isStingyMode() {
        return stingyMode;
    }

    /** @param stingyMode 짠돌이 모드 활성화 여부 */
    public void setStingyMode(boolean stingyMode) {
        this.stingyMode = stingyMode;
    }

    /** @return 치킨 1마리 기준 단가 */
    public int getChickenUnitPrice() {
        return chickenUnitPrice;
    }

    /** @param chickenUnitPrice 치킨 1마리 기준 단가(1 이상) */
    public void setChickenUnitPrice(int chickenUnitPrice) {
        this.chickenUnitPrice = chickenUnitPrice;
    }
}
