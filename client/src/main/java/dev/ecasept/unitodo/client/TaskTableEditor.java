package dev.ecasept.unitodo.client;

import dev.ecasept.unitodo.shared.utils.Log;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TaskTableEditor extends AbstractCellEditor implements TableCellEditor {

    private Object currentValue;
    private JButton button;
    private JCheckBox checkBox;
    private MainFrame frame;
    private JTable currentTable;
    private JButton titleButton;
    private int currentColumn;

    private static final String TAG = "TaskTableEditor";

    public TaskTableEditor(MainFrame frame) {
        this.frame = frame;
        button = new JButton();
        checkBox = new JCheckBox();
        titleButton = new JButton();

        checkBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = currentTable.getEditingRow();
                if (row != -1) {
                    stopCellEditing();
                    frame.changeStateTaskClicked(row);
                }
            }
        });


        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = currentTable.getEditingRow();
                if (row != -1) {
                    stopCellEditing();

                    if (currentColumn == 1) {
                        frame.showTaskClicked(row);
                    } else if (currentColumn == 4) {
                        frame.editTaskClicked(row);
                    } else if (currentColumn == 5) {
                        frame.deleteTaskClicked(row);
                    }
                }
            }
        });


        titleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = currentTable.getEditingRow();
                if (row != -1) {
                    stopCellEditing();
                    frame.showTaskClicked(row);
                }
            }
        });

    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.currentValue = value;
        this.currentTable = table;
        this.currentColumn = column;

        if (column == 0) {
            checkBox.setSelected(true);
            return checkBox;
        }

        if (column == 1) {
            titleButton.setText("");
            return titleButton;
        }

        if (column == 4) {
            var imgUrl = TaskTableEditor.class.getClassLoader().getResource("edit.png");
            if (imgUrl != null) {
                button.setIcon(new ImageIcon(imgUrl));
            } else {
                Log.e(TAG, "Failed to load edit icon");
            }
            button.setText("X");
            return button;
        }

        if (column == 5) {
            var imgUrl = TaskTableEditor.class.getClassLoader().getResource("delete.png");
            if (imgUrl != null) {
                button.setIcon(new ImageIcon(imgUrl));
            } else {
                Log.e(TAG, "Failed to load delete icon");
            }
            button.setText("X");
            return button;
        }


        JLabel label = new JLabel("");
        label.setOpaque(true);
        return label;
    }

    @Override
    public Object getCellEditorValue() {
        return currentValue;

    }




}
