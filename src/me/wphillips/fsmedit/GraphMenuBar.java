package me.wphillips.fsmedit;

import javax.swing.*;

/**
 * Simple menu bar used by {@link GraphEditor} containing "File" and "Edit" menus.
 */
public class GraphMenuBar extends JMenuBar {
    private final GraphPanel panel;

    public GraphMenuBar(GraphPanel panel) {
        this.panel = panel;
        JMenu fileMenu = new JMenu("File");

        JMenuItem openItem = new JMenuItem("Open...");
        openItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                try {
                    panel.loadGraph(chooser.getSelectedFile());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel,
                            "Failed to open file: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem("Save...");
        saveItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(panel) == JFileChooser.APPROVE_OPTION) {
                try {
                    panel.saveGraph(chooser.getSelectedFile());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel,
                            "Failed to save file: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        fileMenu.add(saveItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        JMenu editMenu = new JMenu("Edit");

        add(fileMenu);
        add(editMenu);
    }
}
