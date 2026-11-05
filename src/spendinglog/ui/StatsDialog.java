package spendinglog.ui;

import spendinglog.manager.LedgerManager;
import spendinglog.manager.StatService;
import spendinglog.model.Record;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * 기간별 필터링과 월간 통계 차트를 보여주는 다이얼로그(유스케이스 2).
 *
 * <p>조회 단위(연/월/주/일)와 기간을 선택하면 {@link LedgerManager#filterByPeriod}로
 * 내역을 걸러낸 뒤 {@link StatService#calcCategoryRatio}로 카테고리별 비율을 구해
 * {@link CategoryChartPanel}에 막대그래프로 표시한다.</p>
 *
 * @author 황시은 (255096)
 */
public class StatsDialog extends JDialog {

    private final LedgerManager ledger;
    private final StatService stat;

    private final JComboBox<String> unitBox =
            new JComboBox<>(new String[]{"연", "월", "주", "일"});
    private final JTextField periodField =
            new JTextField(YearMonth.now().toString(), 8);
    private final CategoryChartPanel chart = new CategoryChartPanel();

    /**
     * 통계 다이얼로그를 생성한다.
     *
     * @param owner  부모 프레임
     * @param ledger 내역 매니저
     * @param stat   통계 서비스
     */
    public StatsDialog(Frame owner, LedgerManager ledger, StatService stat) {
        super(owner, "기간별 통계", true);
        this.ledger = ledger;
        this.stat = stat;
        buildUI();
        refresh();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("조회 단위:"));
        unitBox.setSelectedItem("월");
        top.add(unitBox);
        top.add(new JLabel("기간:"));
        top.add(periodField);
        JButton viewBtn = new JButton("통계 보기");
        viewBtn.addActionListener(e -> refresh());
        top.add(viewBtn);

        unitBox.addActionListener(e -> updateHint());

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(chart, BorderLayout.CENTER);
    }

    private void updateHint() {
        String unit = (String) unitBox.getSelectedItem();
        switch (unit) {
            case "연" -> periodField.setText(String.valueOf(LocalDate.now().getYear()));
            case "월" -> periodField.setText(YearMonth.now().toString());
            default -> periodField.setText(LocalDate.now().toString());
        }
    }

    /** 선택한 단위/기간으로 내역을 필터링하고 차트를 다시 그린다. */
    private void refresh() {
        String unit = (String) unitBox.getSelectedItem();
        String period = periodField.getText().trim();
        LocalDate from;
        LocalDate to;
        try {
            switch (unit) {
                case "연" -> {
                    int year = Integer.parseInt(period);
                    from = LocalDate.of(year, 1, 1);
                    to = LocalDate.of(year, 12, 31);
                }
                case "월" -> {
                    YearMonth ym = YearMonth.parse(period);
                    from = ym.atDay(1);
                    to = ym.atEndOfMonth();
                }
                case "주" -> {
                    LocalDate d = LocalDate.parse(period);
                    from = d.minusDays(d.getDayOfWeek().getValue() - 1L);
                    to = from.plusDays(6);
                }
                default -> {
                    LocalDate d = LocalDate.parse(period);
                    from = d;
                    to = d;
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "기간 형식을 확인해 주세요. (연: 2026, 월: 2026-09, 주/일: 2026-09-30)",
                    "입력 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Record> filtered = ledger.filterByPeriod(unit, from, to);
        if (filtered.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    period + " 기간의 내역이 없습니다.", "안내",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        Map<String, Double> ratio = stat.calcCategoryRatio(filtered);
        long total = filtered.stream()
                .filter(r -> r.getType() == Record.Type.EXPENSE)
                .mapToLong(Record::getAmount).sum();
        chart.setData(ratio, total);
    }
}
