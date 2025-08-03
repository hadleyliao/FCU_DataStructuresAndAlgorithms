/***********************
 * 對應課程: Chapter 2
 * CourseWork1: 課表程式
 ***********************/

package _20250715;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;

public class Timetable extends JFrame {
    // 課表JTable
    private JTable table;
    // JTable的資料模型
    private DefaultTableModel model;
    // 星期欄位名稱
    private static final String[] DAYS = {"星期一", "星期二", "星期三", "星期四", "星期五"};
    // 節次數量
    private static final int PERIODS = 6;
    // 課程名稱陣列，索引即為課程編號
    private static final String[] COURSE_NAMES = {"", "計算機概論", "離散數學", "資料結構", "資料庫理論", "上機實習"};
    // 用於保存課表編號（數字陣列）
    private int[][] codeArray = new int[PERIODS][DAYS.length];

    public Timetable() {
        setTitle("課表"); // 設定視窗標題
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 關閉視窗時結束程式
        setSize(800, 350); // 設定視窗大小
        setLocationRelativeTo(null); // 視窗置中

        // 建立課程清單 JList，顯示所有課名
        DefaultListModel<String> courseListModel = new DefaultListModel<>();
        for (int i = 1; i < COURSE_NAMES.length; i++) {
            courseListModel.addElement(COURSE_NAMES[i]);
        }
        JList<String> courseList = new JList<>(courseListModel);
        courseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 單選
        courseList.setDragEnabled(true); // 啟用拖拉
        // 設定TransferHandler，支援拖拉課名到表格
        courseList.setTransferHandler(new TransferHandler() {
            @Override
            public int getSourceActions(JComponent c) {
                return COPY;
            }
            @Override
            protected Transferable createTransferable(JComponent c) {
                JList<?> list = (JList<?>) c;
                Object value = list.getSelectedValue();
                if (value != null) {
                    return new StringSelection(value.toString());
                }
                return null;
            }
        });
        JScrollPane courseScrollPane = new JScrollPane(courseList);
        courseScrollPane.setPreferredSize(new Dimension(120, 200));
        courseScrollPane.setBorder(BorderFactory.createTitledBorder("課程清單"));

        // 建立表格欄位名稱，第一欄為節次，其餘為星期
        String[] columnNames = new String[DAYS.length + 1];
        columnNames[0] = "節次";
        System.arraycopy(DAYS, 0, columnNames, 1, DAYS.length);

        // 初始化表格資料，第一欄為節次編號，其餘為空字串
        Object[][] data = new Object[PERIODS][DAYS.length + 1];
        for (int i = 0; i < PERIODS; i++) {
            data[i][0] = i + 1; // 節次
            for (int j = 1; j <= DAYS.length; j++) {
                data[i][j] = "";
            }
        }

        // 建立表格資料模型，僅節次欄不可編輯
        model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };
        // 建立JTable並設定外觀
        table = new JTable(model);
        table.setRowHeight(40); // 放大格子高度
        table.setFont(new Font("SansSerif", Font.PLAIN, 20)); // 放大格子內文字
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 20)); // 放大表頭文字
        table.setDropMode(DropMode.ON); // 支援拖放
        table.setTransferHandler(new TableTransferHandler()); // 支援拖拉課名進表格
        JScrollPane scrollPane = new JScrollPane(table);

        // 主面板，左側為課程清單，右側為課表
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(courseScrollPane, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // 保存按鈕，將課表內容輸出到控制台
        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> printTableToConsole());
        // 數字⟺課名轉換按鈕，切換課表顯示模式
        JButton convertButton = new JButton("數字⟺課名轉換");
        convertButton.addActionListener(e -> toggleNumbersAndNames());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(convertButton);

        // 顯示課名與編號對照表
        StringBuilder mapping = new StringBuilder("課名與編號對照：  ");
        for (int i = 1; i < COURSE_NAMES.length; i++) {
            mapping.append(i).append(".").append(COURSE_NAMES[i]);
            if (i < COURSE_NAMES.length - 1) mapping.append("   ");
        }
        JLabel mappingLabel = new JLabel(mapping.toString());
        mappingLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JPanel mappingPanel = new JPanel();
        mappingPanel.add(mappingLabel);

        // 將按鈕與對照表放在視窗下方
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonPanel, BorderLayout.NORTH);
        southPanel.add(mappingPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);
    }

    // JTable拖拉處理器，允許拖拉課名到可編輯格子
    private class TableTransferHandler extends TransferHandler {
        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDrop()) return false;
            JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
            int col = dl.getColumn();
            // 只允許拖到可編輯格子
            return col > 0;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) return false;
            JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
            int row = dl.getRow();
            int col = dl.getColumn();
            try {
                String data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                model.setValueAt(data, row, col);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    // 輸出課表內容與課表編號陣列到控制台
    private void printTableToConsole() {
        // 輸出表頭
        for (int i = 0; i < model.getColumnCount(); i++) {
            System.out.print(model.getColumnName(i) + "\t");
        }
        System.out.println();
        // 輸出每一行（課名）
        for (int row = 0; row < model.getRowCount(); row++) {
            for (int col = 0; col < model.getColumnCount(); col++) {
                Object value = model.getValueAt(row, col);
                System.out.print(value + "\t");
            }
            System.out.println();
        }
        // 輸出課表編號陣列
        System.out.println("\n課表編號陣列:");
        for (int row = 0; row < PERIODS; row++) {
            for (int col = 0; col < DAYS.length; col++) {
                System.out.print(codeArray[row][col] + (col == DAYS.length - 1 ? "" : ", "));
            }
            System.out.println();
        }
    }

    // 數字與課名互轉功能，支援手動輸入數字或課名
    private boolean isNameMode = true; // true: 顯示課名, false: 顯示數字
    private void toggleNumbersAndNames() {
        if (isNameMode) {
            // 課名→數字 或 數字→數字（保留數字）
            for (int row = 0; row < PERIODS; row++) {
                for (int col = 1; col <= DAYS.length; col++) {
                    Object value = model.getValueAt(row, col);
                    int code = 0;
                    if (value != null && !value.toString().isEmpty()) {
                        String str = value.toString();
                        // 若是課名
                        boolean found = false;
                        for (int i = 1; i < COURSE_NAMES.length; i++) {
                            if (COURSE_NAMES[i].equals(str)) {
                                code = i;
                                found = true;
                                break;
                            }
                        }
                        // 若是數字
                        if (!found && str.matches("[1-5]")) {
                            code = Integer.parseInt(str);
                        }
                    }
                    model.setValueAt(code == 0 ? "" : String.valueOf(code), row, col);
                    codeArray[row][col - 1] = code;
                }
            }
        } else {
            // 數字→課名
            for (int row = 0; row < PERIODS; row++) {
                for (int col = 1; col <= DAYS.length; col++) {
                    Object value = model.getValueAt(row, col);
                    int code = 0;
                    if (value != null && value.toString().matches("[1-5]")) {
                        code = Integer.parseInt(value.toString());
                    }
                    model.setValueAt(code == 0 ? "" : COURSE_NAMES[code], row, col);
                    codeArray[row][col - 1] = code;
                }
            }
        }
        isNameMode = !isNameMode;
    }

    // 主程式進入點，啟動GUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Timetable().setVisible(true);
        });
    }
}

