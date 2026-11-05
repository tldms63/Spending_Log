package spendinglog.ui;

import spendinglog.manager.BudgetManager;
import spendinglog.manager.LedgerManager;
import spendinglog.manager.StatService;
import spendinglog.manager.WishlistManager;
import spendinglog.model.Budget;
import spendinglog.model.Record;
import spendinglog.model.WishlistItem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.util.List;

/**
 * 소비로그(Spending-Log)의 메인 윈도우.
 *
 * <p>시퀀스 다이어그램 전반의 {@code UI:MainFrame}에 대응하며, 모든 매니저를
 * 생성·보유하고 사용자 입력을 각 매니저로 전달한다. 화면은 UI 디자인대로
 * 메뉴 바, 예산 진행 바, 내역 테이블(JTable), 기능 버튼 패널, 상태 표시줄로
 * 구성된다. 프로그램 종료 시 데이터를 파일에 저장하고, 시작 시 복원한다
 * (유스케이스 3).</p>
 *
 * @author 황시은 (255096)
 */
public class MainFrame extends JFrame {

    /** 데이터 저장 파일 경로. */
    private static final String DATA_FILE = "spendinglog_data.txt";

    private final LedgerManager ledger = new LedgerManager();
    private final WishlistManager wishlist = new WishlistManager();
    private final StatService stat = new StatService();
    private Budget budget;
    private BudgetManager budgetManager;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final BudgetBar budgetBar = new BudgetBar();
    private final JLabel budgetLabel = new JLabel();
    private final JLabel statusLabel = new JLabel();
    private final JButton stingyToggle = new JButton();
    private final JPanel rootPanel;

    private boolean saved = false;

    /** 메인 프레임을 생성하고 저장된 데이터를 복원한다. */
    public MainFrame() {
        super("소비로그 (Spending-Log)");

        // 저장 데이터 복원 (유스케이스 3)
        budget = ledger.getFileStore().loadBudget(DATA_FILE);
        budgetManager = new BudgetManager(budget);
        int loaded = ledger.loadData(DATA_FILE);
        wishlist.setItems(ledger.getFileStore().loadWishlist(DATA_FILE));

        tableModel = new DefaultTableModel(
                new Object[]{"날짜", "분류", "금액", "결제", "태그"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);

        buildMenuBar();
        rootPanel = buildContent();
        setContentPane(rootPanel);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });

        refreshTable();
        refreshBudgetBar();
        updateStatus(loaded > 0 ? "지난 기록 " + loaded + "건을 불러왔습니다." : "새 가계부를 시작합니다.");
        updateStingyButton();

        setSize(720, 560);
        setLocationRelativeTo(null);
    }

    private void buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("파일");
        JMenuItem saveItem = new JMenuItem("저장");
        JMenuItem loadItem = new JMenuItem("불러오기");
        JMenuItem exitItem = new JMenuItem("종료");
        saveItem.addActionListener(e -> doSave(true));
        loadItem.addActionListener(e -> doLoad());
        exitItem.addActionListener(e -> onExit());
        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu viewMenu = new JMenu("보기");
        JMenuItem statsItem = new JMenuItem("기간·통계 보기");
        statsItem.addActionListener(e -> openStats());
        viewMenu.add(statsItem);

        JMenu settingMenu = new JMenu("설정");
        JMenuItem budgetItem = new JMenuItem("예산 설정");
        JMenuItem chickenItem = new JMenuItem("치킨 단가 설정");
        budgetItem.addActionListener(e -> openBudgetSettings());
        chickenItem.addActionListener(e -> openChickenSettings());
        settingMenu.add(budgetItem);
        settingMenu.add(chickenItem);

        JMenu helpMenu = new JMenu("도움말");
        JMenuItem aboutItem = new JMenuItem("소비로그 정보");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "소비로그 (Spending-Log)\n황시은 (255096)\n전남대학교 인공지능학부\n\n"
                        + "지루한 가계부를 재미있게, 소비하는 순간 개입하는 능동형 자산 관리 앱",
                "소비로그 정보", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(settingMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // 상단: 예산 진행 바 (UI 요소 2)
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xB0, 0xB0)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        budgetLabel.setVerticalAlignment(SwingConstants.CENTER);
        topPanel.add(budgetLabel, BorderLayout.WEST);
        topPanel.add(budgetBar, BorderLayout.CENTER);
        root.add(topPanel, BorderLayout.NORTH);

        // 중앙: 내역 테이블 (UI 요소 3)
        table.setRowHeight(24);
        table.getColumnModel().getColumn(2).setCellRenderer(new AmountRenderer());
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        // 우측: 기능 버튼 패널 (UI 요소 4)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        JButton addBtn = fnButton("+ 내역 추가", e -> openAddRecord());
        JButton statsBtn = fnButton("기간·통계 보기", e -> openStats());
        JButton wishBtn = fnButton("위시리스트", e -> openWishlist());
        JButton chickenBtn = fnButton("치킨 지수 보기", e -> openChickenIndex());
        JButton editBtn = fnButton("내역 수정", e -> editSelected());
        JButton delBtn = fnButton("내역 삭제", e -> deleteSelected());
        stingyToggle.addActionListener(e -> toggleStingy());
        styleFnButton(stingyToggle);

        buttonPanel.add(addBtn);
        buttonPanel.add(Box.createVerticalStrut(6));
        buttonPanel.add(statsBtn);
        buttonPanel.add(Box.createVerticalStrut(6));
        buttonPanel.add(wishBtn);
        buttonPanel.add(Box.createVerticalStrut(6));
        buttonPanel.add(chickenBtn);
        buttonPanel.add(Box.createVerticalStrut(6));
        buttonPanel.add(editBtn);
        buttonPanel.add(Box.createVerticalStrut(6));
        buttonPanel.add(delBtn);
        buttonPanel.add(Box.createVerticalStrut(6));
        buttonPanel.add(stingyToggle);
        root.add(buttonPanel, BorderLayout.EAST);

        // 하단: 상태 표시줄 (UI 요소 5)
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 2, 4));
        statusLabel.setForeground(new Color(0x66, 0x66, 0x66));
        root.add(statusLabel, BorderLayout.SOUTH);

        return root;
    }

    private JButton fnButton(String text, java.awt.event.ActionListener action) {
        JButton b = new JButton(text);
        b.addActionListener(action);
        styleFnButton(b);
        return b;
    }

    private void styleFnButton(JButton b) {
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(130, 32));
        b.setPreferredSize(new Dimension(130, 32));
    }

    // ---------- 유스케이스 1: 내역 등록·관리 ----------

    private void openAddRecord() {
        RecordDialog dlg = new RecordDialog(this);
        dlg.setVisible(true);
        Record r = dlg.getResult();
        if (r == null) {
            return;
        }
        long before = budgetManager.monthlySpent(ledger.getRecords(), LocalDate.now());
        if (!ledger.addRecord(r)) {
            JOptionPane.showMessageDialog(this,
                    "저장에 실패했습니다. 입력값을 확인해 주세요.", "오류",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        refreshTable();
        refreshBudgetBar();
        updateStatus(String.format("[%s] %s %,d원 저장 완료.",
                r.getDate(), r.getCategory(), r.getSignedAmount()));

        // 유스케이스 4: 예산 초과 경고 (지출일 때만)
        if (r.getType() == Record.Type.EXPENSE) {
            checkBudgetAfterAdd(before, (int) r.getAmount(), r.getCategory());
        }
    }

    private void checkBudgetAfterAdd(long before, int amount, String category) {
        long after = before + amount;
        boolean over = budgetManager.checkBudget(amount, category, before);
        boolean stingyWarn = budgetManager.isStingyWarning(after);

        if (stingyWarn) {
            // 짠돌이 모드: 배경 적색 + 강력 경고
            flashRedBackground();
            JOptionPane.showMessageDialog(this,
                    String.format("⚠ 경고! 지출 %,d원이 경고 예산 %,d원(한도)을 넘었습니다.\n"
                                    + "이대로면 예산 %,d원을 초과할 수 있어요!",
                            after, budget.getWarningLimit(), budget.getMonthlyBudget()),
                    "짠돌이 모드 경고", JOptionPane.ERROR_MESSAGE);
        } else if (over) {
            JOptionPane.showMessageDialog(this,
                    String.format("예산 초과! %,d원 중 %,d원 사용 (%d%%).",
                            budget.getMonthlyBudget(), after,
                            Math.round(budgetManager.usageRatio(after) * 100)),
                    "예산 경고", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void flashRedBackground() {
        Color original = rootPanel.getBackground();
        rootPanel.setBackground(new Color(0xF2, 0xB0, 0xB0));
        Timer t = new Timer(900, e -> rootPanel.setBackground(original));
        t.setRepeats(false);
        t.start();
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "수정할 내역을 선택하세요.");
            return;
        }
        Record existing = ledger.getRecords().get(row);
        RecordDialog dlg = new RecordDialog(this, existing);
        dlg.setVisible(true);
        Record r = dlg.getResult();
        if (r == null) {
            return;
        }
        if (ledger.updateRecord(row, r)) {
            refreshTable();
            refreshBudgetBar();
            updateStatus("내역을 수정했습니다.");
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "삭제할 내역을 선택하세요.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "선택한 내역을 삭제할까요?",
                "내역 삭제", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION && ledger.deleteRecord(row)) {
            refreshTable();
            refreshBudgetBar();
            updateStatus("내역을 삭제했습니다.");
        }
    }

    // ---------- 유스케이스 2: 기간별 통계 ----------

    private void openStats() {
        new StatsDialog(this, ledger, stat).setVisible(true);
    }

    // ---------- 유스케이스 5: 위시리스트 ----------

    private void openWishlist() {
        new WishlistDialog(this, wishlist).setVisible(true);
    }

    // ---------- 유스케이스 6: 치킨 지수 ----------

    private void openChickenIndex() {
        String[] cats = {"전체", "식비", "교통", "쇼핑", "문화", "기타"};
        String choice = (String) JOptionPane.showInputDialog(this,
                "치킨 지수로 환산할 대상을 고르세요:", "치킨 지수 보기",
                JOptionPane.PLAIN_MESSAGE, null, cats, cats[0]);
        if (choice == null) {
            return;
        }
        long sum;
        if ("전체".equals(choice)) {
            sum = ledger.getRecords().stream()
                    .filter(r -> r.getType() == Record.Type.EXPENSE)
                    .mapToLong(Record::getAmount).sum();
        } else {
            sum = ledger.getRecords().stream()
                    .filter(r -> r.getType() == Record.Type.EXPENSE)
                    .filter(r -> choice.equals(r.getCategory()))
                    .mapToLong(Record::getAmount).sum();
        }
        long chicken = stat.toChickenIndex(sum, budget.getChickenUnitPrice());
        JOptionPane.showMessageDialog(this,
                String.format("%s 지출 %,d원 = 🍗 치킨 %d마리!", choice, sum, chicken),
                "치킨 지수", JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------- 유스케이스 4: 짠돌이 모드 / 예산 설정 ----------

    private void toggleStingy() {
        budget.setStingyMode(!budget.isStingyMode());
        updateStingyButton();
        refreshBudgetBar();
        updateStatus("짠돌이 모드를 " + (budget.isStingyMode() ? "켰습니다." : "껐습니다."));
    }

    private void updateStingyButton() {
        stingyToggle.setText(budget.isStingyMode() ? "짠돌이 모드 ON" : "짠돌이 모드 OFF");
    }

    private void openBudgetSettings() {
        JTextField monthlyField = new JTextField(String.valueOf(budget.getMonthlyBudget()));
        JTextField warnField = new JTextField(String.valueOf(budget.getWarningLimit()));
        JPanel p = new JPanel(new GridLayout(2, 2, 6, 6));
        p.add(new JLabel("월 총예산:"));
        p.add(monthlyField);
        p.add(new JLabel("경고 한도(짠돌이):"));
        p.add(warnField);
        int ok = JOptionPane.showConfirmDialog(this, p, "예산 설정",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            budget.setMonthlyBudget(Long.parseLong(monthlyField.getText().trim()));
            budget.setWarningLimit(Long.parseLong(warnField.getText().trim()));
            refreshBudgetBar();
            updateStatus("예산을 변경했습니다.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "숫자로 입력하세요.");
        }
    }

    private void openChickenSettings() {
        String input = JOptionPane.showInputDialog(this,
                "치킨 1마리 기준 단가(원):", budget.getChickenUnitPrice());
        if (input == null) {
            return;
        }
        try {
            int price = Integer.parseInt(input.trim());
            if (price <= 0) {
                throw new NumberFormatException();
            }
            budget.setChickenUnitPrice(price);
            updateStatus("치킨 단가를 " + String.format("%,d원", price) + "으로 설정했습니다.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "1 이상의 숫자로 입력하세요.");
        }
    }

    // ---------- 유스케이스 3: 저장·불러오기 ----------

    private void doSave(boolean notify) {
        boolean ok = ledger.saveData(DATA_FILE, wishlist.getItems(), budget);
        saved = ok;
        if (notify) {
            if (ok) {
                updateStatus("저장했습니다. (" + DATA_FILE + ")");
            } else {
                JOptionPane.showMessageDialog(this,
                        "저장에 실패했습니다. 다시 시도해 주세요.", "저장 오류",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doLoad() {
        int n = ledger.loadData(DATA_FILE);
        wishlist.setItems(ledger.getFileStore().loadWishlist(DATA_FILE));
        budget = ledger.getFileStore().loadBudget(DATA_FILE);
        budgetManager = new BudgetManager(budget);
        refreshTable();
        refreshBudgetBar();
        updateStingyButton();
        JOptionPane.showMessageDialog(this,
                "지난 기록 " + n + "건을 불러왔습니다.", "불러오기",
                JOptionPane.INFORMATION_MESSAGE);
        updateStatus("불러오기 완료: " + n + "건");
    }

    private void onExit() {
        doSave(false);
        dispose();
        System.exit(0);
    }

    // ---------- 화면 갱신 ----------

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Record r : ledger.getRecords()) {
            tableModel.addRow(new Object[]{
                    r.getDate().toString(),
                    (r.getType() == Record.Type.INCOME ? "수입" : r.getCategory()),
                    String.format("%,d", r.getSignedAmount()),
                    r.getPayment(),
                    r.getHashtag()
            });
        }
    }

    private void refreshBudgetBar() {
        long spent = budgetManager.monthlySpent(ledger.getRecords(), LocalDate.now());
        double ratio = budgetManager.usageRatio(spent);
        double warnRatio = budget.getMonthlyBudget() > 0
                ? (double) budget.getWarningLimit() / budget.getMonthlyBudget() : -1;
        budgetBar.update(ratio, budget.isStingyMode(), warnRatio);
        budgetLabel.setText(String.format("<html><b>이번 달</b><br>지출 %,d / 예산 %,d원</html>",
                spent, budget.getMonthlyBudget()));
    }

    private void updateStatus(String msg) {
        statusLabel.setText(String.format("총 %d건 · %s", ledger.getRecords().size(), msg));
    }

    /**
     * 지출 금액은 빨강, 수입 금액은 파랑으로 표시하는 셀 렌더러(UI 요소 3).
     */
    private static class AmountRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            String text = value == null ? "" : value.toString();
            if (text.startsWith("-")) {
                c.setForeground(new Color(0xC0, 0x39, 0x2B));
            } else {
                c.setForeground(new Color(0x1F, 0x5F, 0xC0));
            }
            setHorizontalAlignment(SwingConstants.RIGHT);
            return c;
        }
    }
}
