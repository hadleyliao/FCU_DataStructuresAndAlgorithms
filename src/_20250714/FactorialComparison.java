package _20250714;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FactorialComparison extends JFrame {
    private JTextField inputField;
    private JTextArea resultArea;

    public FactorialComparison() {
        setTitle("N! 遞迴與迴圈比較");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("請輸入N: "));
        inputField = new JTextField(10);
        inputPanel.add(inputField);
        JButton calcButton = new JButton("計算");
        inputPanel.add(calcButton);
        JButton clearButton = new JButton("清除");
        inputPanel.add(clearButton);
        add(inputPanel, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        calcButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateFactorials();
            }
        });
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resultArea.setText("");
            }
        });
    }

    private void calculateFactorials() {
        String input = inputField.getText();
        try {
            int n = Integer.parseInt(input);
            if (n < 0) {
                resultArea.append("N必須為非負整數\n");
                return;
            }
            // 迴圈法
            long startLoop = System.nanoTime();
            long loopResult = factorialLoop(n);
            long endLoop = System.nanoTime();
            long loopTime = endLoop - startLoop;
            String loopMsg;
            if (loopResult < 0) {
                loopMsg = "[迴圈法]\n結果: 數字過大計算有誤\n";
            } else {
                loopMsg = "[迴圈法]\n結果: " + loopResult + "\n";
            }
            loopMsg += "執行時間: " + loopTime + " ns\n";

            // 遞迴法
            long recResult = 0;
            String recMsg = "";
            long startRec = System.nanoTime();
            try {
                recResult = factorialRecursive(n);
                long endRec = System.nanoTime();
                long recTime = endRec - startRec;
                if (recResult < 0) {
                    recMsg = "[遞迴法]\n結果: 數字過大計算有誤\n";
                } else {
                    recMsg = "[遞迴法]\n結果: " + recResult + "\n";
                }
                recMsg += "執行時間: " + recTime + " ns\n";
            } catch (StackOverflowError err) {
                recMsg = "[遞迴法]\n結果: 數字過大計算有誤(StackOverflow)\n";
            }
            resultArea.append("N = " + n + "\n" + loopMsg + recMsg + "\n");
        } catch (NumberFormatException ex) {
            resultArea.append("請輸入正確的整數\n");
        }
    }

    private long factorialLoop(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    private long factorialRecursive(int n) {
        if (n == 0 || n == 1) return 1;
        return n * factorialRecursive(n - 1);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FactorialComparison().setVisible(true);
        });
    }
}
