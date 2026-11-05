package spendinglog.ui;

import spendinglog.manager.WishlistManager;
import spendinglog.model.WishlistItem;

import javax.swing.*;
import java.awt.*;

/**
 * 위시리스트 항목별 저축 진행을 관리하는 다이얼로그(유스케이스 5).
 *
 * <p>목표 물건별로 탭을 만들고, 각 탭에 '저축액/목표가'와 달성률 바를 표시한다.
 * '저축 배정' 버튼으로 금액을 더하면 {@link WishlistManager#updateSaving}이
 * 달성률을 갱신하고 진행 바를 다시 그린다. 100% 도달 시 축하 메시지를 띄운다.</p>
 *
 * @author 황시은 (255096)
 */
public class WishlistDialog extends JDialog {

    private final WishlistManager wishlist;
    private final JTabbedPane tabs = new JTabbedPane();

    /**
     * 위시리스트 다이얼로그를 생성한다.
     *
     * @param owner    부모 프레임
     * @param wishlist 위시리스트 매니저
     */
    public WishlistDialog(Frame owner, WishlistManager wishlist) {
        super(owner, "위시리스트 저축", true);
        this.wishlist = wishlist;
        buildUI();
        reloadTabs();
        setSize(460, 360);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);

        JButton addBtn = new JButton("항목 추가");
        JButton assignBtn = new JButton("저축 배정");
        JButton removeBtn = new JButton("항목 삭제");

        addBtn.addActionListener(e -> onAdd());
        assignBtn.addActionListener(e -> onAssign());
        removeBtn.addActionListener(e -> onRemove());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(addBtn);
        buttons.add(assignBtn);
        buttons.add(removeBtn);
        add(buttons, BorderLayout.SOUTH);
    }

    private void reloadTabs() {
        tabs.removeAll();
        for (WishlistItem item : wishlist.getItems()) {
            tabs.addTab(item.getName(), buildItemPanel(item));
        }
        if (wishlist.getItems().isEmpty()) {
            JPanel empty = new JPanel(new GridBagLayout());
            empty.add(new JLabel("'항목 추가'로 사고 싶은 물건을 등록하세요."));
            tabs.addTab("비어 있음", empty);
        }
    }

    private JPanel buildItemPanel(WishlistItem item) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel(String.format("<html><b>%s</b><br>%,d / %,d원</html>",
                item.getName(), item.getSaved(), item.getTargetPrice()));
        panel.add(title, BorderLayout.NORTH);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) Math.round(item.getProgress() * 100));
        bar.setStringPainted(true);
        bar.setString((int) Math.round(item.getProgress() * 100) + "%");
        bar.setPreferredSize(new Dimension(300, 120));
        bar.setOrientation(SwingConstants.VERTICAL);
        bar.setForeground(new Color(0xC0, 0x4A, 0x4A));

        JPanel barWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        barWrap.add(bar);
        panel.add(barWrap, BorderLayout.CENTER);

        return panel;
    }

    private void onAdd() {
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JPanel p = new JPanel(new GridLayout(2, 2, 6, 6));
        p.add(new JLabel("물건 이름:"));
        p.add(nameField);
        p.add(new JLabel("목표 가격:"));
        p.add(priceField);

        int ok = JOptionPane.showConfirmDialog(this, p, "위시리스트 항목 추가",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "물건 이름을 입력하세요.");
            return;
        }
        long price;
        try {
            price = Long.parseLong(priceField.getText().trim());
            if (price <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "목표 가격은 1 이상의 숫자로 입력하세요.");
            return;
        }
        wishlist.addItem(name, price);
        reloadTabs();
        tabs.setSelectedIndex(tabs.getTabCount() - 1);
    }

    private void onAssign() {
        WishlistItem item = currentItem();
        if (item == null) {
            return;
        }
        String input = JOptionPane.showInputDialog(this,
                item.getName() + "에 배정할 저축액을 입력하세요:", "저축 배정",
                JOptionPane.PLAIN_MESSAGE);
        if (input == null) {
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(input.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "숫자로 입력하세요.");
            return;
        }
        double progress = wishlist.updateSaving(item.getName(), amount);
        reloadTabs();
        selectTabByName(item.getName());
        if (progress >= 1.0) {
            JOptionPane.showMessageDialog(this,
                    "🎉 축하합니다! '" + item.getName() + "' 목표를 달성했어요!",
                    "목표 달성", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onRemove() {
        WishlistItem item = currentItem();
        if (item == null) {
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "'" + item.getName() + "' 항목을 삭제할까요?", "항목 삭제",
                JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            wishlist.removeItem(item);
            reloadTabs();
        }
    }

    private WishlistItem currentItem() {
        int idx = tabs.getSelectedIndex();
        if (idx < 0 || idx >= wishlist.getItems().size()) {
            return null;
        }
        return wishlist.getItems().get(idx);
    }

    private void selectTabByName(String name) {
        for (int i = 0; i < tabs.getTabCount(); i++) {
            if (name.equals(tabs.getTitleAt(i))) {
                tabs.setSelectedIndex(i);
                return;
            }
        }
    }
}
