package dev.ecasept.unitodo.client.ui.component;

import dev.ecasept.unitodo.client.ui.icon.PriorityIcon;
import dev.ecasept.unitodo.shared.models.db.TaskPriority;
import dev.ecasept.unitodo.shared.utils.Log;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * This class is responsible for rendering the cell of the table that displays the tasks.
 */
public class TaskTableCellRenderer implements TableCellRenderer {

    private static final String TAG = "TaskTableCellRenderer";

    /**
     * This method returns a Component that ist used to draw a cell in the JTable the method is called for.
     *
     * @param table           the JTable of which the cells are drawn.
     * @param value           the value of the cell to be rendered.
     * @param isSelected      true if the cell is to be rendered with the
     *                          selection highlighted; otherwise false.
     * @param hasFocus        if true, render cell appropriately.
     * @param row             the row index of the cell being drawn.
     * @param column          the column index of the cell being drawn.
     *
     * @return the component used for drawing the cell.
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        // Renderer für Spalte 0
        if (column == 0) {



            String str = (String) value;
            if (value.equals("Erledigt")) {
                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected(true);
                checkBox.setOpaque(true);
                return checkBox;
            } else {
                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected(false);
                checkBox.setOpaque(true);
                return checkBox;
            }
        }

        // Renderer für Spalte 1
        if (column == 1) {
            JLabel label = new JLabel((String) value);
            label.setOpaque(true);
            return label;
        }

        // Renderer für Spalte 2
        if (column == 2) {
            JLabel label = new JLabel((String) value);
            label.setOpaque(true);
            return label;
        }

        // Renderer für Spalte 3
        if (column == 3) {
            JLabel label = new JLabel();
            label.setOpaque(true);
            TaskPriority priority = (TaskPriority) value;

            if (value == TaskPriority.High) {
                label.setIcon(new PriorityIcon(Color.RED, 20));
                return label;
            } else if (value == TaskPriority.Mid) {
                label.setIcon(new PriorityIcon(Color.YELLOW, 20));
                return label;
            } else if (value == TaskPriority.Low) {
                return label;
            }
        }

        // Renderer für Spalte 4
        if (column == 4) {
            JButton button = new JButton((String) value);
            button.setOpaque(true);
            var imgUrl = TaskTableCellRenderer.class.getClassLoader().getResource("edit.png");
            if (imgUrl != null) {
                button.setIcon(new ImageIcon(imgUrl));
            } else {
                Log.e(TAG, "Failed to load edit icon");
            }
            return button;
        }


        // Renderer für Spalte 5
        if (column == 5) {
            JButton button = new JButton((String) value);
            button.setOpaque(true);
            var imgUrl = TaskTableCellRenderer.class.getClassLoader().getResource("delete.png");
            if (imgUrl != null) {
                button.setIcon(new ImageIcon(imgUrl));
            } else {
                Log.e(TAG, "Failed to load delete icon");
            }
            return button;
        }




        JLabel label = new JLabel();
        label.setOpaque(true);
        return label;
    }
}
