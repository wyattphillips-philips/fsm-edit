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
    private final JMenuItem copyItem;
    private final JMenuItem pasteItem;

    public GraphPopupMenu(GraphPanel panel) {
        this.panel = panel;

        JMenuItem addNodeItem = new JMenuItem("Add Node");
        addNodeItem.addActionListener(e -> {
            int wx = panel.screenToWorldX(x);
            int wy = panel.screenToWorldY(y);
            Node node = new Node(wx, wy, 30, "N" + (panel.getNodeCount() + 1));
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
                panel.clearSelection();
            }
        });
        add(deleteNodeItem);

        copyItem = new JMenuItem("Copy");
        copyItem.addActionListener(e -> panel.copyContext(targetNode));
        add(copyItem);

        pasteItem = new JMenuItem("Paste");
        pasteItem.addActionListener(e -> {
            int wx = panel.screenToWorldX(x);
            int wy = panel.screenToWorldY(y);
            panel.pasteClipboard(wx, wy);
        });
        add(pasteItem);
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
        boolean hasSelection = !panel.getSelectedNodes().isEmpty();
        copyItem.setVisible(node != null || hasSelection);
        pasteItem.setVisible(panel.hasClipboard());
        show(invoker, x, y);
    }
}
