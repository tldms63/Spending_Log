package spendinglog.manager;

import spendinglog.model.Budget;
import spendinglog.model.Record;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * 예산 상태를 관리하고 예산 초과 여부를 판단하는 객체.
 *
 * <p>시퀀스 다이어그램 SD-2(예산 초과 경고 / 짠돌이 모드)의
 * {@code budget:BudgetManager}에 대응한다. 이번 달 누적 지출에 새 지출을
 * 더해 월 예산 초과 여부를 판단하고(요구사항 9), 짠돌이 모드에서는
 * 권장 지출 한도 초과 시 강한 경고를 트리거한다(요구사항 10).</p>
 *
 * @author 황시은 (255096)
 */
public class BudgetManager {

    private final Budget budget;

    /**
     * 예산 설정을 받아 매니저를 생성한다.
     *
     * @param budget 공유 예산 설정 객체
     */
    public BudgetManager(Budget budget) {
        this.budget = budget;
    }

    /** @return 현재 예산 설정 */
    public Budget getBudget() {
        return budget;
    }

    /**
     * 이번 달(기준일이 속한 월)의 누적 지출액을 계산한다.
     *
     * @param records 전체 내역 목록
     * @param refDate 기준 날짜(보통 오늘)
     * @return 해당 월 지출 합계(원)
     */
    public long monthlySpent(List<Record> records, LocalDate refDate) {
        YearMonth ym = YearMonth.from(refDate);
        long sum = 0;
        for (Record r : records) {
            if (r.getType() == Record.Type.EXPENSE && YearMonth.from(r.getDate()).equals(ym)) {
                sum += r.getAmount();
            }
        }
        return sum;
    }

    /**
     * 기존 월 누계에 이번 지출액을 더해 월 예산 초과 여부를 판단한다
     * (유스케이스 4). 초과 시 UI가 경고 팝업을, 짠돌이 모드에서는
     * 배경 적색 전환과 강력 경고를 띄우도록 true를 반환한다.
     *
     * @param amount      이번 지출액(원)
     * @param category    카테고리(향후 카테고리별 한도 확장 지점)
     * @param currentSpent 이번 달 현재까지의 누적 지출액
     * @return 예산 초과 여부(true=초과)
     */
    public boolean checkBudget(int amount, String category, long currentSpent) {
        long projected = currentSpent + amount;
        return projected > budget.getMonthlyBudget();
    }

    /**
     * 짠돌이 모드 경고를 띄워야 하는지 판단한다.
     * 짠돌이 모드가 켜져 있고, 누계가 경고 한도(권장 지출)를 넘으면 true.
     *
     * @param currentSpent 이번 달 현재까지의 누적 지출액
     * @return 짠돌이 경고 트리거 여부
     */
    public boolean isStingyWarning(long currentSpent) {
        return budget.isStingyMode() && currentSpent > budget.getWarningLimit();
    }

    /**
     * 이번 달 지출이 월 예산에서 차지하는 비율(0.0~, 1.0=100%)을 반환한다.
     * 메인 화면의 예산 진행 바에 사용된다.
     *
     * @param currentSpent 이번 달 누적 지출액
     * @return 예산 소진 비율
     */
    public double usageRatio(long currentSpent) {
        if (budget.getMonthlyBudget() <= 0) {
            return 0.0;
        }
        return (double) currentSpent / budget.getMonthlyBudget();
    }
}
