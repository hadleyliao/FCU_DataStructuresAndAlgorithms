/*************************
 * 對應課程: Chapter 4
 * CourseWork2: 中序轉後序前序
 **************************/

package _20250724;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;
import java.awt.image.BufferedImage;

public class InfixConverterGUI extends JFrame {
    private JTextField inputField;
    private JTextField prefixField;
    private JTextField postfixField;
    private JButton convertButton;
    private JButton clearButton;
    private JLabel statusLabel;

    // 現代化配色方案
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color LIGHT_GRAY = new Color(248, 249, 250);
    private static final Color BORDER_COLOR = new Color(206, 212, 218);
    private static final Color TEXT_COLOR = new Color(52, 58, 64);

    public InfixConverterGUI() {
        initializeComponents();
        setupLayout();
        addEventListeners();
        setupKeyBindings();
    }

    private void initializeComponents() {
        setTitle("中序 ⇌ 前序/後序 表達式轉換器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(600, 350));

        // 設置應用程式圖標
        try {
            setIconImage(createIcon());
        } catch (Exception e) {
            // 忽略圖標設置錯誤
        }

        // 創建輸入欄位
        inputField = createStyledTextField();
        inputField.setToolTipText("輸入中序表達式，例如: (A+B)*C-D/E");

        // 創建結果欄位
        prefixField = createStyledTextField();
        prefixField.setEditable(false);
        prefixField.setBackground(LIGHT_GRAY);
        prefixField.setToolTipText("前序表達式結果");

        postfixField = createStyledTextField();
        postfixField.setEditable(false);
        postfixField.setBackground(LIGHT_GRAY);
        postfixField.setToolTipText("後序表達式結果");

        // 創建按鈕
        convertButton = createStyledButton("轉換", PRIMARY_COLOR);
        convertButton.setToolTipText("點擊或按 Enter 進行轉換");

        clearButton = createStyledButton("清空", DANGER_COLOR);
        clearButton.setToolTipText("清空所有欄位");

        // 狀態標籤
        statusLabel = new JLabel("準備就緒");
        statusLabel.setFont(new Font("微軟正黑體", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(108, 117, 125));
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Consolas", Font.PLAIN, 16));
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.setPreferredSize(new Dimension(0, 40));
        return field;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("微軟正黑體", Font.BOLD, 20)); // 字體更大
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(80, 50)); // 按鈕大小調整
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 添加懸停效果
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private void setupLayout() {
        setLayout(new BorderLayout(0, 0));

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        mainPanel.setBackground(Color.WHITE);

        // 標題面板
        JPanel titlePanel = createTitlePanel();

        // 輸入面板
        JPanel inputPanel = createInputPanel();

        // 按鈕面板
        JPanel buttonPanel = createButtonPanel();

        // 狀態面板
        JPanel statusPanel = createStatusPanel();

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("中序表達式轉換器");
        titleLabel.setFont(new Font("微軟正黑體", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);

        JLabel subtitleLabel = new JLabel("支援 +、-、*、/、^ 運算符及括號");
        subtitleLabel.setFont(new Font("微軟正黑體", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(108, 117, 125));

        panel.add(titleLabel);
        panel.add(Box.createHorizontalStrut(Integer.MAX_VALUE));

        JPanel titleContainer = new JPanel(new BorderLayout());
        titleContainer.setBackground(Color.WHITE);
        titleContainer.add(titleLabel, BorderLayout.CENTER);
        titleContainer.add(subtitleLabel, BorderLayout.SOUTH);

        return titleContainer;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 0, 12, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 輸入行
        addInputRow(panel, gbc, 0, "中序式 (Infix):", inputField);
        addInputRow(panel, gbc, 1, "前序式 (Prefix):", prefixField);
        addInputRow(panel, gbc, 2, "後序式 (Postfix):", postfixField);

        return panel;
    }

    private void addInputRow(JPanel parent, GridBagConstraints gbc, int row, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("微軟正黑體", Font.BOLD, 14));
        label.setForeground(TEXT_COLOR);

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.25;
        parent.add(label, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0.75;
        parent.add(field, gbc);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        panel.add(convertButton);
        panel.add(clearButton);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(new EmptyBorder(8, 15, 8, 15));
        panel.add(statusLabel, BorderLayout.WEST);

        // 添加幫助信息
        JLabel helpLabel = new JLabel("快捷鍵: Enter-轉換 | Ctrl+L-清空 | Esc-退出");
        helpLabel.setFont(new Font("微軟正黑體", Font.PLAIN, 11));
        helpLabel.setForeground(new Color(108, 117, 125));
        panel.add(helpLabel, BorderLayout.EAST);

        return panel;
    }

    private void addEventListeners() {
        convertButton.addActionListener(this::performConversion);
        clearButton.addActionListener(this::clearFields);

        // 輸入欄位監聽器
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performConversion(null);
                }
            }
        });

        // 實時驗證
        inputField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateInput(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validateInput(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateInput(); }
        });
    }

    private void setupKeyBindings() {
        // 全局快捷鍵
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "convert");
        getRootPane().getActionMap().put("convert", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { performConversion(e); }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), "clear");
        getRootPane().getActionMap().put("clear", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { clearFields(e); }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
        getRootPane().getActionMap().put("exit", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { System.exit(0); }
        });
    }

    private void performConversion(ActionEvent e) {
        String infix = inputField.getText().trim();

        if (infix.isEmpty()) {
            showStatus("請輸入中序表達式", DANGER_COLOR);
            inputField.requestFocus();
            return;
        }

        try {
            String postfix = infixToPostfix(infix);
            String prefix = infixToPrefix(infix);

            postfixField.setText(postfix);
            prefixField.setText(prefix);
            showStatus("轉換成功!", SUCCESS_COLOR);
        } catch (Exception ex) {
            postfixField.setText("");
            prefixField.setText("");
            showStatus("輸入格式錯誤: " + ex.getMessage(), DANGER_COLOR);
            JOptionPane.showMessageDialog(this,
                    "輸入格式錯誤!\n\n請檢查:\n• 括號是否配對\n• 運算符是否正確\n• 運算元是否有效",
                    "轉換錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields(ActionEvent e) {
        inputField.setText("");
        prefixField.setText("");
        postfixField.setText("");
        showStatus("已清空所有欄位", new Color(108, 117, 125));
        inputField.requestFocus();
    }

    private void validateInput() {
        String input = inputField.getText().trim();
        if (input.isEmpty()) {
            showStatus("準備就緒", new Color(108, 117, 125));
        } else if (isValidExpression(input)) {
            showStatus("表達式格式正確", SUCCESS_COLOR);
        } else {
            showStatus("表達式格式可能有誤", new Color(255, 193, 7));
        }
    }

    private boolean isValidExpression(String expr) {
        int parentheses = 0;
        for (char c : expr.toCharArray()) {
            if (c == '(') parentheses++;
            else if (c == ')') parentheses--;
            if (parentheses < 0) return false;
        }
        return parentheses == 0;
    }

    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private Image createIcon() {
        // 創建簡單的應用程式圖標
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(PRIMARY_COLOR);
        g2.fillRoundRect(2, 2, 28, 28, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("fx", 8, 22);
        g2.dispose();
        return (Image) img;
    }

    // 支援一元負號的中序轉後序（正統標記法：一元負號直接用 - 表示）
    private String infixToPostfix(String infix) {
        StringBuilder output = new StringBuilder();
        Stack<Character> stack = new Stack<>();
        boolean lastWasOpOrLeft = true; // 用於判斷一元負號
        for (int i = 0; i < infix.length(); i++) {
            char c = infix.charAt(i);
            if (c == ' ') continue;
            if (Character.isLetterOrDigit(c)) {
                output.append(c);
                lastWasOpOrLeft = false;
            } else if (c == '(') {
                stack.push(c);
                lastWasOpOrLeft = true;
            } else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    output.append(stack.pop());
                }
                if (stack.isEmpty()) throw new RuntimeException("括號不匹配");
                stack.pop();
                lastWasOpOrLeft = false;
            } else if (isOperator(c)) {
                if (c == '-' && lastWasOpOrLeft) {
                    // 一元負號，直接用 - 表示
                    stack.push('`'); // 用特殊符號暫存，稍後替換為 -
                } else {
                    while (!stack.isEmpty() && precedence(c) <= precedence(stack.peek())) {
                        output.append(stack.pop());
                    }
                    stack.push(c);
                }
                lastWasOpOrLeft = true;
            } else if (c != ' ') {
                throw new RuntimeException("無效字符: " + c);
            }
        }
        while (!stack.isEmpty()) {
            char op = stack.pop();
            if (op == '(' || op == ')') throw new RuntimeException("括號不匹配");
            output.append(op);
        }
        // 將 ` 替換為 -（正統標記法）
        return output.toString().replace("`", "-");
    }

    // 支援一元負號的中序轉前序（正統標記法：一元負號直接用 - 表示）
    private String infixToPrefix(String infix) {
        Stack<String> operators = new Stack<>();
        Stack<String> operands = new Stack<>();
        boolean lastWasOpOrLeft = true;
        for (int i = 0; i < infix.length(); i++) {
            char c = infix.charAt(i);
            if (c == ' ') continue;
            if (Character.isLetterOrDigit(c)) {
                operands.push(String.valueOf(c));
                lastWasOpOrLeft = false;
            } else if (c == '(') {
                operators.push("(");
                lastWasOpOrLeft = true;
            } else if (c == ')') {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    String op = operators.pop();
                    if (operands.size() < (op.equals("`") ? 1 : 2)) throw new RuntimeException("運算元不足");
                    String b = operands.pop();
                    String a = op.equals("`") ? "" : operands.pop();
                    operands.push(op + a + b);
                }
                if (operators.isEmpty()) throw new RuntimeException("括號不匹配");
                operators.pop();
                lastWasOpOrLeft = false;
            } else if (isOperator(c)) {
                if (c == '-' && lastWasOpOrLeft) {
                    operators.push("`"); // 一元負號暫存
                } else {
                    while (!operators.isEmpty() &&
                            (precedence(c) < precedence(operators.peek().charAt(0)) ||
                                    (precedence(c) == precedence(operators.peek().charAt(0)) && c != '^'))) {
                        if (operators.peek().equals("(")) break;
                        String op = operators.pop();
                        if (operands.size() < (op.equals("`") ? 1 : 2)) throw new RuntimeException("運算元不足");
                        String b = operands.pop();
                        String a = op.equals("`") ? "" : operands.pop();
                        operands.push(op + a + b);
                    }
                    operators.push(String.valueOf(c));
                }
                lastWasOpOrLeft = true;
            } else {
                throw new RuntimeException("無效字符: " + c);
            }
        }
        while (!operators.isEmpty()) {
            String op = operators.pop();
            if (op.equals("(") || op.equals(")")) throw new RuntimeException("括號不匹配");
            if (operands.size() < (op.equals("`") ? 1 : 2)) throw new RuntimeException("運算元不足");
            String b = operands.pop();
            String a = op.equals("`") ? "" : operands.pop();
            operands.push(op + a + b);
        }
        if (operands.isEmpty()) throw new RuntimeException("無效表達式");
        // 將 ` 替換為 -（正統標記法）
        return operands.pop().replace("`", "-");
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    private int precedence(char c) {
        switch (c) {
            case '+':
            case '-': return 1;
            case '*':
            case '/': return 2;
            case '^': return 3;
        }
        return -1;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new InfixConverterGUI().setVisible(true);
        });
    }
}