package _20250701;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GCDLCMCalculator extends JFrame {

    private JTextField numberField1;
    private JTextField numberField2;
    private JLabel gcdLabel;
    private JLabel lcmLabel;
    private JLabel commonDivisorsLabel;

    public GCDLCMCalculator() {
        setTitle("最大公因數與最小公倍數計算器");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 視窗置中

        // 使用 GridBagLayout 排版
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 輸入欄位1
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("輸入第一個整數："), gbc);

        numberField1 = new JTextField(10);
        gbc.gridx = 1;
        add(numberField1, gbc);

        // 輸入欄位2
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("輸入第二個整數："), gbc);

        numberField2 = new JTextField(10);
        gbc.gridx = 1;
        add(numberField2, gbc);

        // 按鈕
        JButton calcButton = new JButton("計算 GCD 和 LCM");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        add(calcButton, gbc);

        // 結果顯示
        gcdLabel = new JLabel("最大公因數 (GCD)：");
        lcmLabel = new JLabel("最小公倍數 (LCM)：");

        gbc.gridy = 3;
        add(gcdLabel, gbc);

        gbc.gridy = 4;
        add(lcmLabel, gbc);

        // 顯示所有公因數
        commonDivisorsLabel = new JLabel("所有公因數：");
        gbc.gridy = 5;
        add(commonDivisorsLabel, gbc);

        // 事件處理
        calcButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calculate();
            }
        });
    }

    private void calculate() {
        try {
            int num1 = Integer.parseInt(numberField1.getText().trim());
            int num2 = Integer.parseInt(numberField2.getText().trim());

            int gcd = findGCD(num1, num2);
            int lcm = Math.abs(num1 * num2) / gcd;

            gcdLabel.setText("最大公因數 (GCD)： " + gcd);
            lcmLabel.setText("最小公倍數 (LCM)： " + lcm);
            commonDivisorsLabel.setText("所有公因數：" + getCommonDivisors(num1, num2));

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "請輸入有效的整數", "錯誤", JOptionPane.ERROR_MESSAGE);
        } catch (ArithmeticException ex) {
            JOptionPane.showMessageDialog(this, "無法計算（可能輸入為 0）", "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int findGCD(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    private String getCommonDivisors(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
        int min = Math.min(a, b);
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= min; i++) {
            if (a % i == 0 && b % i == 0) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(i);
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        // 正確啟動 Swing GUI 的方式（避免 UI 錯亂）
        SwingUtilities.invokeLater(() -> {
            GCDLCMCalculator frame = new GCDLCMCalculator();
            frame.setVisible(true);
        });
    }
}
