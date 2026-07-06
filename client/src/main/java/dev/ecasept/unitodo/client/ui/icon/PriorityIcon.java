package dev.ecasept.unitodo.client.ui.icon;

import javax.swing.*;
import java.awt.*;

/**
 * This class is responsible for drawing the priority icon of every task in the JTable displaying all tasks.
 * The priority-icon is a filled square colored red if the priority of the task is high,
 * colored yellow if the priority of the task is mid and colored white if the priority
 * of the task ist low.
 */
public class PriorityIcon implements Icon {
    private final Color color;
    private final int size;

    /**
     * Creates a new PriorityIcon-object
     * @param color the color the square
     * @param size the size of the square
     */
    public PriorityIcon(Color color, int size) {
        this.color = color;
        this.size = size;

    }

    /**
     * Paints the square signaling the priority of the task.
     *
     * @param c  a {@code Component} to get properties useful for painting.
     * @param g  the graphics context.
     * @param x  the X coordinate of the icon's top-left corner.
     * @param y  the Y coordinate of the icon's top-left corner.
     */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(color);
        g.drawRect(30, 10, size, size);
        g.fillRect(30, 10, size, size);
    }

    /**
     * Returns the width of the drawn square.
     * @return returns the width of the drawn square.
     */
    @Override
    public int getIconWidth() {
        return size;
    }

    /**
     * Returns the height of the drawn square.
     * @return returns the height of the drawn square.
     */
    @Override
    public int getIconHeight() {
        return size;
    }
}
