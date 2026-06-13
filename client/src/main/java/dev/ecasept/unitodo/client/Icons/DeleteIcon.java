package dev.ecasept.unitodo.client.Icons;

import javax.swing.*;
import java.awt.*;

public class DeleteIcon implements Icon {

    private final Color color;
    private final int size;

    public DeleteIcon(Color color, int size) {
        this.color = color;
        this.size = size;

    }


    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(color);
        g.drawRect(15, 10, size, size);
        g.setColor(Color.RED);
        g.drawLine(15, 10, size + 15, size + 10);
        g.drawLine(15 + size, 10, 15, size + 10);


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

