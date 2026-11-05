package spendinglog;

import spendinglog.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * 소비로그(Spending-Log) 애플리케이션의 진입점.
 *
 * <p>시스템 기본 Look&amp;Feel을 적용한 뒤 Swing 이벤트 디스패치 스레드에서
 * {@link MainFrame}을 띄운다.</p>
 *
 * @author 황시은 (255096)
 */
public final class Main {

    private Main() {
    }

    /**
     * 프로그램을 시작한다.
     *
     * @param args 사용하지 않음
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {
            // 기본 L&F로 계속 진행
        }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
