import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;

public class FactorialComparison2 extends JFrame {
    private JTextField inputField;
    private JTextArea resultArea;

    public FactorialComparison2() {
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
            // 迴圈法 (使用BigInteger)
            long startLoop = System.nanoTime();
            BigInteger loopResult = factorialLoopBig(n);
            long endLoop = System.nanoTime();
            long loopTime = endLoop - startLoop;
            String loopMsg = "[迴圈法]\n結果: " + loopResult.toString() + "\n";
            loopMsg += "執行時間: " + loopTime + " ns\n";

            // 遞迴法 (使用BigInteger)
            BigInteger recResult = BigInteger.ZERO;
            String recMsg = "";
            long startRec = System.nanoTime();
            try {
                recResult = factorialRecursiveBig(n);
                long endRec = System.nanoTime();
                long recTime = endRec - startRec;
                recMsg = "[遞迴法]\n結果: " + recResult.toString() + "\n";
                recMsg += "執行時間: " + recTime + " ns\n";
            } catch (StackOverflowError err) {
                recMsg = "[遞迴法]\n結果: 數字過大計算有誤(StackOverflow)\n";
            }
            resultArea.append("N = " + n + "\n" + loopMsg + recMsg + "\n");
        } catch (NumberFormatException ex) {
            resultArea.append("請輸入正確的整數\n");
        }
    }

    /**
     * 迴圈法計算N!，使用BigInteger避免溢位
     */
    private BigInteger factorialLoopBig(int n) {
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }

    /**
     * 遞迴法計算N!，使用BigInteger避免溢位
     */
    private BigInteger factorialRecursiveBig(int n) {
        if (n == 0 || n == 1) return BigInteger.ONE;
        return BigInteger.valueOf(n).multiply(factorialRecursiveBig(n - 1));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FactorialComparison2().setVisible(true);
        });
    }
}
