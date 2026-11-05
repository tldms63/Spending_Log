package spendinglog.manager;

import spendinglog.model.Record;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 통계 계산을 담당하는 서비스 객체.
 *
 * <p>시퀀스 다이어그램 SD-3(기간별 통계)과 SD-6(치킨 지수 변환)의
 * {@code stat:StatService}에 대응한다. 카테고리별 지출 비율을 구해
 * 차트 데이터로 제공하고(유스케이스 2), 지출액을 치킨 마리 수라는
 * 직관적 단위로 환산한다(유스케이스 6).</p>
 *
 * @author 황시은 (255096)
 */
public class StatService {

    /**
     * 주어진 내역 목록에서 카테고리별 지출 비율(%)을 계산한다.
     * 지출(EXPENSE) 내역만 집계하며, 비율은 전체 지출 대비 백분율이다.
     *
     * @param records 조회된 내역 목록
     * @return 카테고리 → 비율(%) 맵. 입력에 지출이 없으면 빈 맵.
     */
    public Map<String, Double> calcCategoryRatio(List<Record> records) {
        Map<String, Long> sumByCategory = new LinkedHashMap<>();
        long total = 0;

        for (Record r : records) {
            if (r.getType() != Record.Type.EXPENSE) {
                continue;
            }
            String cat = r.getCategory() == null ? "기타" : r.getCategory();
            sumByCategory.merge(cat, r.getAmount(), Long::sum);
            total += r.getAmount();
        }

        Map<String, Double> ratio = new LinkedHashMap<>();
        if (total == 0) {
            return ratio;
        }
        for (Map.Entry<String, Long> e : sumByCategory.entrySet()) {
            ratio.put(e.getKey(), e.getValue() * 100.0 / total);
        }
        return ratio;
    }

    /**
     * 카테고리별 지출 합계(원)를 반환한다. 차트의 절대 금액 표시에 쓰인다.
     *
     * @param records 조회된 내역 목록
     * @return 카테고리 → 지출 합계(원) 맵
     */
    public Map<String, Long> calcCategorySum(List<Record> records) {
        Map<String, Long> sumByCategory = new LinkedHashMap<>();
        for (Record r : records) {
            if (r.getType() != Record.Type.EXPENSE) {
                continue;
            }
            String cat = r.getCategory() == null ? "기타" : r.getCategory();
            sumByCategory.merge(cat, r.getAmount(), Long::sum);
        }
        return sumByCategory;
    }

    /**
     * 지출액을 치킨 1마리 단가로 나눠 치킨 마리 수로 환산한다(유스케이스 6).
     *
     * @param amount    지출액(원)
     * @param unitPrice 치킨 1마리 가격(원, 1 이상)
     * @return 치킨 마리 수(내림). 단가가 0 이하이면 0.
     */
    public int toChickenIndex(int amount, int unitPrice) {
        if (unitPrice <= 0) {
            return 0;
        }
        return amount / unitPrice;
    }

    /**
     * {@code long} 지출액용 오버로드. 큰 금액도 안전하게 환산한다.
     *
     * @param amount    지출액(원)
     * @param unitPrice 치킨 1마리 가격(원, 1 이상)
     * @return 치킨 마리 수(내림)
     */
    public long toChickenIndex(long amount, int unitPrice) {
        if (unitPrice <= 0) {
            return 0;
        }
        return amount / unitPrice;
    }
}
