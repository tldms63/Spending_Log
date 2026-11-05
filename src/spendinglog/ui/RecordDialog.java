package spendinglog.ui;

import spendinglog.model.Record;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * 수입/지출 내역을 입력·수정하는 모달 다이얼로그(유스케이스 1, UI 디자인).
 *
 * <p>날짜, 금액, 카테고리, 결제수단, 해시태그, 메모, 구분(지출/수입)을 입력받는다.
 * 금액에 숫자가 아닌 값이 들어오면 "숫자로 입력해 주세요" 경고 후 저장을 취소한다
 * (유스케이스 1 예외 1).</p>
 *
 * @author 황시은 (255096)
 */
public class RecordDialog extends JDialog {

    private final JTextField dateField = new JTextField(LocalDate.now().toString());
    private final JTextField amountField = new JTextField();
    private final JComboBox<String> categoryBox =
            new JComboBox<>(new String[]{"식비", "교통", "쇼핑", "문화", "기타"});
    private final JComboBox<String> paymentBox =
            new JComboBox<>(new String[]{"현금", "카드", "계좌"});
    private final JTextField hashtagField = new JTextField();
    private final JTextField memoField = new JTextField();
    private final JRadioButton expenseRadio = new JRadioButton("지출", true);
    private final JRadioButton incomeRadio = new JRadioButton("수입");

    private Record result;

    /**
     * 새 내역 입력용 다이얼로그를 생성한다.
     *
     * @param owner 부모 프레임
     */
    public RecordDialog(Frame owner) {
        this(owner, null);
    }

    /**
     * 기존 내역을 편집하는 다이얼로그를 생성한다.
     *
     * @param owner    부모 프레임
     * @param existing 수정할 내역(null이면 신규)
     */
    public RecordDialog(Frame owner, Record existing) {
        super(owner, existing == null ? "내역 추가" : "내역 수정", true);
        buildUI();
        if (existing != null) {
            dateField.setText(existing.getDate().toString());
            amountField.setText(String.valueOf(existing.getAmount()));
            categoryBox.setSelectedItem(existing.getCategory());
            paymentBox.setSelectedItem(existing.getPayment());
            hashtagField.setText(existing.getHashtag());
            memoField.setText(existing.getMemo());
            if (existing.getType() == Record.Type.INCOME) {
                incomeRadio.setSelected(true);
            } else {
                expenseRadio.setSelected(true);
            }
        }
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(form, c, row++, "날짜", dateField);
        addRow(form, c, row++, "금액", amountField);
        addRow(form, c, row++, "카테고리", categoryBox);
        addRow(form, c, row++, "결제수단", paymentBox);
        addRow(form, c, row++, "해시태그", hashtagField);
        addRow(form, c, row++, "메모", memoField);

        ButtonGroup bg = new ButtonGroup();
        bg.add(expenseRadio);
        bg.add(incomeRadio);
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        typePanel.add(expenseRadio);
        typePanel.add(incomeRadio);
        addRow(form, c, row++, "구분", typePanel);

        JButton saveBtn = new JButton("저장");
        JButton cancelBtn = new JButton("취소");
        saveBtn.addActionListener(e -> onSave());
        cancelBtn.addActionListener(e -> {
            result = null;
            dispose();
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(saveBtn);
        buttons.add(cancelBtn);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        dateField.setColumns(16);
        amountField.setColumns(16);
        hashtagField.setColumns(16);
        memoField.setColumns(16);
    }

    private void addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0;
        p.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1;
        p.add(field, c);
    }

    private void onSave() {
        // 금액 숫자 검증 (유스케이스 1 예외 1)
        long amount;
        try {
            amount = Long.parseLong(amountField.getText().trim());
            if (amount < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "금액은 숫자로 입력해 주세요.", "입력 오류",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 날짜 검증
        LocalDate date;
        try {
            date = LocalDate.parse(dateField.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "날짜는 yyyy-mm-dd 형식으로 입력해 주세요.", "입력 오류",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Record.Type type = incomeRadio.isSelected() ? Record.Type.INCOME : Record.Type.EXPENSE;
        String tag = hashtagField.getText().trim();
        if (!tag.isEmpty() && !tag.startsWith("#")) {
            tag = "#" + tag;
        }

        result = new Record(date, amount, type,
                (String) categoryBox.getSelectedItem(),
                (String) paymentBox.getSelectedItem(),
                tag, memoField.getText().trim());
        dispose();
    }

    /**
     * 사용자가 저장을 눌러 만들어진 내역을 반환한다.
     *
     * @return 입력된 내역, 취소했으면 null
     */
    public Record getResult() {
        return result;
    }
}
