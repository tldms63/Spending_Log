package spendinglog.ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * 메인 화면 상단의 예산 진행 바(UI 디자인 요소 2).
 *
 * <p>이번 달 지출/예산 비율을 막대로 표시하고, 짠돌이 모드에서는 경고선과
 * 함께 테두리를 붉게 강조한다. {@code BudgetManager.usageRatio}가 넘겨주는
 * 비율을 그대로 그린다.</p>
 *
 * @author 황시은 (255096)
 */
public class BudgetBar extends JPanel {

    private double ratio = 0.0;
    private boolean stingyMode = false;
    private double warningRatio = -1.0;

    /** 진행 바를 생성한다. */
    public BudgetBar() {
        setPreferredSize(new Dimension(400, 28));
    }

    /**
     * 진행 상태를 갱신한다.
     *
     * @param ratio        예산 소진 비율(0.0~, 1.0=100%)
     * @param stingyMode   짠돌이 모드 on/off
     * @param warningRatio 경고 한도의 예산 대비 비율(음수면 선 숨김)
     */
    public void update(double ratio, boolean stingyMode, double warningRatio) {
        this.ratio = ratio;
        this.stingyMode = stingyMode;
        this.warningRatio = warningRatio;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 배경 트랙
        g2.setColor(new Color(0xE8, 0xE8, 0xE8));
        g2.fillRect(0, 0, w, h);

        // 채워진 막대
        int fillW = (int) (Math.min(1.0, ratio) * w);
        boolean over = ratio >= 1.0;
        Color fill;
        if (over) {
            fill = new Color(0xD9, 0x53, 0x4F); // 초과: 빨강
        } else if (stingyMode && ratio >= warningRatio && warningRatio >= 0) {
            fill = new Color(0xE6, 0x7E, 0x22); // 경고 구간: 주황
        } else {
            fill = new Color(0x4A, 0x6C, 0xF0); // 정상: 파랑
        }
        g2.setColor(fill);
        g2.fillRect(0, 0, fillW, h);

        // 경고선(짠돌이 모드일 때)
        if (stingyMode && warningRatio >= 0 && warningRatio <= 1.0) {
            int x = (int) (warningRatio * w);
            g2.setColor(new Color(0xC0, 0x39, 0x2B));
            g2.fillRect(x - 1, 0, 2, h);
        }

        // 퍼센트 텍스트
        String text = (int) Math.round(ratio * 100) + "%";
        g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
        FontMetrics fm = g2.getFontMetrics();
        int tx = w - fm.stringWidth(text) - 8;
        int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
        g2.setColor(new Color(0x33, 0x33, 0x33));
        g2.drawString(text, tx, ty);

        // 짠돌이 모드 테두리 강조
        if (stingyMode) {
            g2.setColor(new Color(0xC0, 0x39, 0x2B));
            g2.drawRect(0, 0, w - 1, h - 1);
        }
    }
}
