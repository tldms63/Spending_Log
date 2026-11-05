package spendinglog.manager;

import spendinglog.model.Record;
import spendinglog.model.WishlistItem;
import spendinglog.model.Budget;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 가계부 내역 전체를 보관하고 조율하는 핵심 매니저.
 *
 * <p>시퀀스 다이어그램 SD-1(내역 등록), SD-3(기간별 통계), SD-5(저장/불러오기)의
 * {@code ledger:LedgerManager}에 대응한다. 내역의 입력·조회·수정·삭제를 책임지고
 * (요구사항 1), 기간별 필터링을 수행하며(요구사항 5), {@link FileStore}에 저장/복원을
 * 위임한다(요구사항 7).</p>
 *
 * @author 황시은 (255096)
 */
public class LedgerManager {

    private final List<Record> records = new ArrayList<>();
    private final FileStore fileStore = new FileStore();

    /** @return 전체 내역 목록 */
    public List<Record> getRecords() {
        return records;
    }

    /**
     * 내역을 검증한 뒤 리스트에 추가한다(유스케이스 1).
     * 금액이 음수이거나 카테고리가 비면 검증에 실패해 false를 반환한다.
     * 함수 명세의 {@code Boolean addRecord(Record record)}에 해당한다.
     *
     * @param record 추가할 내역
     * @return 저장 성공 여부(true=추가됨)
     */
    public boolean addRecord(Record record) {
        if (!validate(record)) {
            return false;
        }
        records.add(record);
        return true;
    }

    /**
     * 내역 유효성을 검증한다. 금액은 0 이상, 카테고리는 필수.
     *
     * @param record 검증할 내역
     * @return 유효하면 true
     */
    public boolean validate(Record record) {
        if (record == null) {
            return false;
        }
        if (record.getAmount() < 0) {
            return false;
        }
        if (record.getCategory() == null || record.getCategory().isBlank()) {
            return false;
        }
        if (record.getDate() == null) {
            return false;
        }
        return true;
    }

    /**
     * 기존 내역을 새 값으로 수정한다(유스케이스 1 예외 3).
     *
     * @param index      수정할 내역의 인덱스
     * @param newRecord  새 값
     * @return 수정 성공 여부
     */
    public boolean updateRecord(int index, Record newRecord) {
        if (index < 0 || index >= records.size() || !validate(newRecord)) {
            return false;
        }
        records.set(index, newRecord);
        return true;
    }

    /**
     * 내역을 삭제한다(유스케이스 1 예외 3).
     *
     * @param index 삭제할 내역의 인덱스
     * @return 삭제 성공 여부
     */
    public boolean deleteRecord(int index) {
        if (index < 0 || index >= records.size()) {
            return false;
        }
        records.remove(index);
        return true;
    }

    /**
     * 지정한 기간 단위와 범위에 포함되는 내역만 선별해 반환한다(유스케이스 2).
     * 함수 명세의 {@code List<Record> filterByPeriod(String unit, Date from, Date to)}에
     * 해당한다. 결과가 없으면 빈 리스트를 반환한다.
     *
     * @param unit 조회 단위("연", "월", "주", "일") — 호출 측에서 from/to를 결정
     * @param from 조회 시작일(포함)
     * @param to   조회 종료일(포함)
     * @return 해당 기간의 내역 목록
     */
    public List<Record> filterByPeriod(String unit, LocalDate from, LocalDate to) {
        List<Record> result = new ArrayList<>();
        for (Record r : records) {
            LocalDate d = r.getDate();
            if ((d.isEqual(from) || d.isAfter(from))
                    && (d.isEqual(to) || d.isBefore(to))) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * 종료 시 현재 상태(내역, 위시리스트, 예산)를 파일에 저장한다(유스케이스 3).
     *
     * @param path     저장 경로
     * @param wishlist 저장할 위시리스트
     * @param budget   저장할 예산
     * @return 저장 성공 여부
     */
    public boolean saveData(String path, List<WishlistItem> wishlist, Budget budget) {
        return fileStore.saveData(path, records, wishlist, budget);
    }

    /**
     * 파일에서 내역을 읽어 현재 리스트를 교체한다(유스케이스 3).
     *
     * @param path 불러올 경로
     * @return 복원된 내역 건수
     */
    public int loadData(String path) {
        List<Record> loaded = fileStore.loadData(path);
        records.clear();
        records.addAll(loaded);
        return records.size();
    }

    /** @return 영속성 담당 FileStore */
    public FileStore getFileStore() {
        return fileStore;
    }
}
