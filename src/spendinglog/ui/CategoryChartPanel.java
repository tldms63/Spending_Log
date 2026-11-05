package spendinglog.ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 카테고리별 지출 비율을 막대그래프로 그리는 패널(유스케이스 2, UI 디자인).
 *
 * <p>{@code StatService.calcCategoryRatio}가 만든 비율 맵을 받아, 카테고리마다
 * 세로 막대를 그리고 상단에 퍼센트를 표시한다.</p>
 *
 * @author 황시은 (255096)
 */
public class CategoryChartPanel extends JPanel {

    private Map<String, Double> ratio = new LinkedHashMap<>();
    private long totalSpent = 0;

    /** 차트 패널을 생성한다. */
    public CategoryChartPanel() {
        setPreferredSize(new Dimension(460, 320));
        setBackground(Color.WHITE);
    }

    /**
     * 차트 데이터를 설정한다.
     *
     * @param ratio      카테고리 → 비율(%) 맵
     * @param totalSpent 전체 지출 합계(상단 표기용)
     */
    public void setData(Map<String, Double> ratio, long totalSpent) {
        this.ratio = ratio == null ? new LinkedHashMap<>() : ratio;
        this.totalSpent = totalSpent;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 총 지출 제목
        g2.setColor(new Color(0x2C, 0x3E, 0x50));
        g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
        g2.drawString(String.format("총 지출 %,d원", totalSpent), 16, 24);

        if (ratio.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.setFont(getFont().deriveFont(Font.PLAIN, 13f));
            String msg = "표시할 지출 내역이 없습니다.";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
            return;
        }

        int n = ratio.size();
        int bottom = h - 40;   // x축 라벨 공간
        int top = 50;          // 제목 + 퍼센트 공간
        int usableH = bottom - top;
        int slot = w / n;
        int barW = Math.min(60, slot / 2);

        double maxRatio = ratio.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);

        g2.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        FontMetrics fm = g2.getFontMetrics();

        int i = 0;
        for (Map.Entry<String, Double> e : ratio.entrySet()) {
            double r = e.getValue();
            int barH = (int) (usableH * (r / maxRatio));
            int cx = slot * i + slot / 2;
            int x = cx - barW / 2;
            int y = bottom - barH;

            // 막대
            g2.setColor(new Color(0xC0, 0x4A, 0x4A));
            g2.fillRect(x, y, barW, barH);

            // 퍼센트
            g2.setColor(new Color(0x33, 0x33, 0x33));
            String pct = Math.round(r) + "%";
            g2.drawString(pct, cx - fm.stringWidth(pct) / 2, y - 6);

            // 카테고리 라벨
            String label = e.getKey();
            g2.drawString(label, cx - fm.stringWidth(label) / 2, bottom + 18);

            i++;
        }

        // x축 선
        g2.setColor(new Color(0xBB, 0xBB, 0xBB));
        g2.drawLine(0, bottom, w, bottom);
    }
}
