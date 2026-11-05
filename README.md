# 소비로그 (Spending-Log)

황시은 (255096) · 전남대학교 인공지능학부
Software System Design — Checkpoint 4 (Beta Release)

지루한 가계부를 재미있게 만들어 꾸준히 쓰게 하고, 소비하는 순간 능동적으로
개입해 행동을 바꾸는 Java Swing 기반 데스크톱 가계부 애플리케이션.

## 요구 환경
- JDK 17 이상 (개발·테스트: OpenJDK 21)
- 별도 서버/네트워크 불필요 (로컬 텍스트 파일 기반, 요구사항 14 이식성)

## 빌드 및 실행

### 소스에서 컴파일
```bash
javac -encoding UTF-8 -d build $(find src -name "*.java")
java -cp build spendinglog.Main
```

### 실행 가능한 Jar로 실행
```bash
java -jar ssd_ip_cp4_2026f_255096_app.jar
```

실행하면 같은 폴더에 `spendinglog_data.txt`가 생성되어 내역이 자동 저장되고,
다음 실행 시 자동으로 복원됩니다 (유스케이스 3).

## 소스 구조

```
src/spendinglog/
├── Main.java                    진입점
├── model/                       도메인 모델
│   ├── Record.java              수입/지출 내역 한 건
│   ├── WishlistItem.java        위시리스트 저축 항목
│   └── Budget.java              예산 설정
├── manager/                     비즈니스 로직 (시퀀스 다이어그램 객체)
│   ├── LedgerManager.java       내역 관리·필터링 (SD-1,3,5)
│   ├── BudgetManager.java       예산 초과·짠돌이 판단 (SD-2)
│   ├── FileStore.java           파일 저장/불러오기 (SD-5)
│   ├── StatService.java         통계·치킨 지수 (SD-3,6)
│   └── WishlistManager.java     위시리스트 저축 (SD-4)
└── ui/                          Swing GUI
    ├── MainFrame.java           메인 화면 (UI:MainFrame)
    ├── RecordDialog.java        내역 추가/수정
    ├── StatsDialog.java         기간별 통계
    ├── WishlistDialog.java      위시리스트 저축
    ├── BudgetBar.java           예산 진행 바
    └── CategoryChartPanel.java  카테고리 막대그래프
```

## 핵심 함수 (Function Specification 매핑)

| 함수 | 담당 클래스 | 유스케이스 |
|---|---|---|
| `boolean addRecord(Record)` | LedgerManager | UC1 내역 등록 |
| `List<Record> filterByPeriod(unit, from, to)` | LedgerManager | UC2 기간 통계 |
| `Map<String,Double> calcCategoryRatio(records)` | StatService | UC2 통계 |
| `boolean saveData / List<Record> loadData` | FileStore / LedgerManager | UC3 저장·불러오기 |
| `boolean checkBudget(amount, category)` | BudgetManager | UC4 예산·짠돌이 |
| `double updateSaving(itemId, amount)` | WishlistManager | UC5 위시리스트 |
| `int toChickenIndex(amount, unitPrice)` | StatService | UC6 치킨 지수 |

## 기능 (11개 → 6개 유스케이스로 통합)
1. 수입·지출 내역 관리 (입력/조회/수정/삭제, 카테고리·결제수단·해시태그)
2. 기간별 필터링 & 월간 통계 (카테고리별 막대그래프)
3. 데이터 파일 저장·불러오기 (손상 시 안전 복구, 요구사항 15)
4. 예산 설정·경고 & 짠돌이 모드 (초과 시 배경 적색 + 강력 경고)
5. 위시리스트 저축 바 (목표가 대비 달성률 시각화)
6. 치킨 지수 변환기 (지출액을 치킨 N마리로 환산)

## 테스트
매니저 로직 스모크 테스트 25개 전부 통과 (내역 검증, 기간 필터, 비율 계산,
예산 판단, 치킨 환산, 위시리스트 저축, 파일 라운드트립, 손상 파일 처리).

## GitHub
https://github.com/tldms63/Spending_Log
