package dev.ecasept.unitodo.client.ui.icon;

import javax.swing.*;
import java.awt.*;

public class PriorityIcon implements Icon {
    private final Color color;
    private final int size;

    public PriorityIcon(Color color, int size) {
        this.color = color;
        this.size = size;

    }


    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(color);
        g.drawRect(30, 10, size, size);
        g.fillRect(30, 10, size, size);
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
