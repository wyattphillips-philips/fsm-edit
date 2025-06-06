package me.wphillips.fsmedit;

import javax.swing.*;
import java.awt.*;

/**
 * Context menu for {@link GraphPanel} defining actions accessible via right-click.
 */
public class GraphPopupMenu extends JPopupMenu {
    private final GraphPanel panel;
    private int x;
    private int y;

    public GraphPopupMenu(GraphPanel panel) {
        this.panel = panel;

        JMenuItem addNodeItem = new JMenuItem("Add Node");
        addNodeItem.addActionListener(e -> {
            Node node = new Node(x, y, 30, "N" + (panel.getNodeCount() + 1));
            panel.addNode(node);
            panel.repaint();
        });
        add(addNodeItem);
    }

    /**
     * Show the popup menu at the specified location and store that location
     * for later use by menu actions.
     */
    public void showMenu(Component invoker, int x, int y) {
        this.x = x;
        this.y = y;
        show(invoker, x, y);
    }
}
