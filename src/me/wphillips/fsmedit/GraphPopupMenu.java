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
    private Node targetNode;
    private final JMenuItem deleteNodeItem;

    public GraphPopupMenu(GraphPanel panel) {
        this.panel = panel;

        JMenuItem addNodeItem = new JMenuItem("Add Node");
        addNodeItem.addActionListener(e -> {
            Node node = new Node(x, y, 30, "N" + (panel.getNodeCount() + 1));
            panel.addNode(node);
            panel.repaint();
        });
        add(addNodeItem);

        deleteNodeItem = new JMenuItem("Delete Node");
        deleteNodeItem.addActionListener(e -> {
            if (targetNode != null) {
                java.util.List<Node> sel = panel.getSelectedNodes();
                if (sel.size() > 1 && sel.contains(targetNode)) {
                    panel.removeNodes(sel);
                } else {
                    panel.removeNode(targetNode);
                }
            }
        });
        add(deleteNodeItem);
    }

    /**
     * Show the popup menu at the specified location and store that location
     * for later use by menu actions.
     */
    public void showMenu(Component invoker, int x, int y) {
        showMenu(invoker, x, y, null);
    }

    /**
     * Display the menu and configure visibility of actions based on context.
     *
     * @param invoker component requesting the menu
     * @param x x-coordinate
     * @param y y-coordinate
     * @param node node under the cursor or {@code null}
     */
    public void showMenu(Component invoker, int x, int y, Node node) {
        this.x = x;
        this.y = y;
        this.targetNode = node;
        deleteNodeItem.setVisible(node != null);
        show(invoker, x, y);
    }
}
