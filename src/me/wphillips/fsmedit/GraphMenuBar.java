package me.wphillips.fsmedit;

import javax.swing.*;

/**
 * Simple menu bar used by {@link GraphEditor} containing "File" and "Edit" menus.
 */
public class GraphMenuBar extends JMenuBar {
    public GraphMenuBar() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        JMenu editMenu = new JMenu("Edit");

        add(fileMenu);
        add(editMenu);
    }
}
