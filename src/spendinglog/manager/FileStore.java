package spendinglog.manager;

import spendinglog.model.Record;
import spendinglog.model.WishlistItem;
import spendinglog.model.Budget;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 가계부 데이터를 로컬 텍스트 파일로 저장하고 불러오는 영속성 담당 객체.
 *
 * <p>시퀀스 다이어그램 SD-5(데이터 저장/불러오기)의 {@code store:FileStore}에
 * 대응한다. 별도 서버나 네트워크 없이 파일 하나로 동작하므로 이식성 요구사항
 * (요구사항 14)을 만족하며, 파일이 없거나 손상되어도 예외를 흡수해 프로그램이
 * 비정상 종료되지 않도록 한다(요구사항 15).</p>
 *
 * <p>저장 포맷은 사람이 읽을 수 있는 라인 기반 텍스트이다.
 * 각 섹션은 머리글로 구분한다:</p>
 * <pre>
 * #BUDGET|월예산|경고한도|짠돌이모드|치킨단가
 * #RECORD|날짜|금액|타입|카테고리|결제수단|해시태그|메모
 * #WISH|이름|목표가|저축액
 * </pre>
 *
 * @author 황시은 (255096)
 */
public class FileStore {

    /** 필드 구분자. 사용자 입력에서는 치환해 충돌을 막는다. */
    private static final String SEP = "|";
    private static final String SEP_ESCAPE = "/#/";

    /**
     * 현재 내역, 위시리스트, 예산 설정을 모두 텍스트 파일로 저장한다.
     *
     * @param path     저장 파일 경로
     * @param records  저장할 내역 목록
     * @param wishlist 저장할 위시리스트 목록
     * @param budget   저장할 예산 설정
     * @return 저장 성공 여부(true=성공)
     */
    public boolean saveData(String path, List<Record> records,
                            List<WishlistItem> wishlist, Budget budget) {
        Path file = Paths.get(path);
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                // 예산 섹션
                bw.write(String.join(SEP,
                        "#BUDGET",
                        String.valueOf(budget.getMonthlyBudget()),
                        String.valueOf(budget.getWarningLimit()),
                        String.valueOf(budget.isStingyMode()),
                        String.valueOf(budget.getChickenUnitPrice())));
                bw.newLine();

                // 내역 섹션
                for (Record r : records) {
                    bw.write(String.join(SEP,
                            "#RECORD",
                            r.getDate().toString(),
                            String.valueOf(r.getAmount()),
                            r.getType().name(),
                            esc(r.getCategory()),
                            esc(r.getPayment()),
                            esc(r.getHashtag()),
                            esc(r.getMemo())));
                    bw.newLine();
                }

                // 위시리스트 섹션
                for (WishlistItem w : wishlist) {
                    bw.write(String.join(SEP,
                            "#WISH",
                            esc(w.getName()),
                            String.valueOf(w.getTargetPrice()),
                            String.valueOf(w.getSaved())));
                    bw.newLine();
                }
            }
            return true;
        } catch (IOException e) {
            // 디스크 문제 등 저장 오류: 경고만 남기고 실패 반환
            System.err.println("[FileStore] 저장 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 저장 파일을 읽어 내역 목록을 복원한다.
     * 파일이 없거나 손상되면 빈 리스트를 반환해 빈 상태로 시작하게 한다.
     *
     * @param path 불러올 파일 경로
     * @return 복원된 내역 목록(실패 시 빈 리스트)
     */
    public List<Record> loadData(String path) {
        List<Record> records = new ArrayList<>();
        Path file = Paths.get(path);
        if (!Files.exists(file)) {
            return records;
        }
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#RECORD")) {
                    continue;
                }
                Record r = parseRecord(line);
                if (r != null) {
                    records.add(r);
                }
            }
        } catch (IOException e) {
            System.err.println("[FileStore] 불러오기 실패: " + e.getMessage());
        }
        return records;
    }

    /**
     * 저장 파일에서 위시리스트 항목을 복원한다.
     *
     * @param path 불러올 파일 경로
     * @return 복원된 위시리스트(실패 시 빈 리스트)
     */
    public List<WishlistItem> loadWishlist(String path) {
        List<WishlistItem> list = new ArrayList<>();
        Path file = Paths.get(path);
        if (!Files.exists(file)) {
            return list;
        }
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#WISH")) {
                    continue;
                }
                String[] t = line.split("\\" + SEP, -1);
                if (t.length < 4) {
                    continue;
                }
                try {
                    WishlistItem w = new WishlistItem(unesc(t[1]), Long.parseLong(t[2]));
                    w.setSaved(Long.parseLong(t[3]));
                    list.add(w);
                } catch (NumberFormatException ignore) {
                    // 손상된 라인은 건너뛴다
                }
            }
        } catch (IOException e) {
            System.err.println("[FileStore] 위시리스트 불러오기 실패: " + e.getMessage());
        }
        return list;
    }

    /**
     * 저장 파일에서 예산 설정을 복원한다.
     *
     * @param path 불러올 파일 경로
     * @return 복원된 예산(없거나 손상 시 기본 예산)
     */
    public Budget loadBudget(String path) {
        Budget budget = new Budget();
        Path file = Paths.get(path);
        if (!Files.exists(file)) {
            return budget;
        }
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#BUDGET")) {
                    continue;
                }
                String[] t = line.split("\\" + SEP, -1);
                if (t.length < 5) {
                    continue;
                }
                try {
                    budget.setMonthlyBudget(Long.parseLong(t[1]));
                    budget.setWarningLimit(Long.parseLong(t[2]));
                    budget.setStingyMode(Boolean.parseBoolean(t[3]));
                    budget.setChickenUnitPrice(Integer.parseInt(t[4]));
                } catch (NumberFormatException ignore) {
                    // 손상 시 기본값 유지
                }
                break;
            }
        } catch (IOException e) {
            System.err.println("[FileStore] 예산 불러오기 실패: " + e.getMessage());
        }
        return budget;
    }

    /**
     * 한 줄을 파싱해 {@link Record}로 만든다. 손상된 라인이면 null을 반환한다.
     *
     * @param line #RECORD 로 시작하는 한 줄
     * @return 복원된 내역 또는 null
     */
    private Record parseRecord(String line) {
        String[] t = line.split("\\" + SEP, -1);
        if (t.length < 8) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(t[1]);
            long amount = Long.parseLong(t[2]);
            Record.Type type = Record.Type.valueOf(t[3]);
            return new Record(date, amount, type,
                    unesc(t[4]), unesc(t[5]), unesc(t[6]), unesc(t[7]));
        } catch (DateTimeParseException | IllegalArgumentException e) {
            // NumberFormatException, enum valueOf 실패 모두 IllegalArgumentException 하위
            return null; // 손상된 라인은 무시
        }
    }

    private String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace(SEP, SEP_ESCAPE);
    }

    private String unesc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace(SEP_ESCAPE, SEP);
    }
}
