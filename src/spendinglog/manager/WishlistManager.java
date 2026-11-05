package spendinglog.manager;

import spendinglog.model.WishlistItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 위시리스트 항목과 저축 진행을 관리하는 객체.
 *
 * <p>시퀀스 다이어그램 SD-4(위시리스트 저축)의 {@code wish:WishlistManager}에
 * 대응한다. 항목을 추가/수정/삭제하고, 저축액을 배정해 달성률을 갱신한다
 * (유스케이스 5, 요구사항 11).</p>
 *
 * @author 황시은 (255096)
 */
public class WishlistManager {

    private final List<WishlistItem> items = new ArrayList<>();

    /** @return 전체 위시리스트 항목(읽기용) */
    public List<WishlistItem> getItems() {
        return items;
    }

    /**
     * 위시리스트 항목을 추가한다.
     *
     * @param name        물건 이름
     * @param targetPrice 목표 가격(1 이상)
     * @return 생성된 항목
     */
    public WishlistItem addItem(String name, long targetPrice) {
        WishlistItem item = new WishlistItem(name, targetPrice);
        items.add(item);
        return item;
    }

    /**
     * 항목을 제거한다.
     *
     * @param item 제거할 항목
     */
    public void removeItem(WishlistItem item) {
        items.remove(item);
    }

    /**
     * 복원된 항목 목록으로 교체한다(파일 불러오기 시 사용).
     *
     * @param loaded 복원된 위시리스트
     */
    public void setItems(List<WishlistItem> loaded) {
        items.clear();
        if (loaded != null) {
            items.addAll(loaded);
        }
    }

    /**
     * 지정한 항목에 저축액을 더하고 달성률(0.0~1.0)을 계산해 반환한다
     * (유스케이스 5). 함수 명세의 {@code updateSaving(String itemId, int amount)}에
     * 해당하며, 여기서는 항목 이름을 식별자로 사용한다.
     *
     * @param itemId 위시리스트 항목 이름
     * @param amount 저축 배정액(원)
     * @return 갱신된 달성률(0.0~1.0). 항목이 없으면 -1.0.
     */
    public double updateSaving(String itemId, int amount) {
        for (WishlistItem item : items) {
            if (item.getName().equals(itemId)) {
                item.addSaving(amount);
                return item.getProgress();
            }
        }
        return -1.0;
    }
}
