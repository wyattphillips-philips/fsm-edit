package me.wphillips.fsmedit;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import me.wphillips.fsmedit.GraphIO;

/**
 * Simple menu bar used by {@link GraphEditor} containing "File" and "Edit" menus.
 */
public class GraphMenuBar extends JMenuBar {
    private final GraphPanel panel;

    private JFileChooser createChooser() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "FSM Graph (*." + GraphIO.EXTENSION + ")",
                GraphIO.EXTENSION);
        chooser.setFileFilter(filter);
        chooser.setAcceptAllFileFilterUsed(false);
        return chooser;
    }

    public GraphMenuBar(GraphPanel panel) {
        this.panel = panel;
        JMenu fileMenu = new JMenu("File");

        JMenuItem newItem = new JMenuItem("New");
        newItem.addActionListener(e -> {
            if (panel.getNodeCount() > 0 || panel.getEdgeCount() > 0) {
                int choice = JOptionPane.showConfirmDialog(panel,
                        "Save current graph before creating a new one?",
                        "Unsaved Graph", JOptionPane.YES_NO_CANCEL_OPTION);
                if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
                    return;
                }
                if (choice == JOptionPane.YES_OPTION) {
                    JFileChooser chooser = createChooser();
                    if (chooser.showSaveDialog(panel) == JFileChooser.APPROVE_OPTION) {
                        try {
                            panel.saveGraph(GraphIO.withExtension(
                                    chooser.getSelectedFile()));
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(panel,
                                    "Failed to save file: " + ex.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }
            panel.clearGraph();
        });
        fileMenu.add(newItem);

        JMenuItem openItem = new JMenuItem("Open...");
        openItem.addActionListener(e -> {
            JFileChooser chooser = createChooser();
            if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                try {
                    panel.loadGraph(GraphIO.withExtension(
                            chooser.getSelectedFile()));
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
            JFileChooser chooser = createChooser();
            if (chooser.showSaveDialog(panel) == JFileChooser.APPROVE_OPTION) {
                try {
                    panel.saveGraph(GraphIO.withExtension(
                            chooser.getSelectedFile()));
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

        JMenu viewMenu = new JMenu("View");
        JMenuItem resetItem = new JMenuItem("Reset");
        resetItem.addActionListener(e -> panel.resetView());
        viewMenu.add(resetItem);

        JCheckBoxMenuItem gridItem = new JCheckBoxMenuItem("Show Grid");
        gridItem.setSelected(panel.isShowGrid());
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        gridItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, mask));
        gridItem.addActionListener(e -> panel.setShowGrid(gridItem.isSelected()));
        viewMenu.add(gridItem);

        add(fileMenu);
        add(editMenu);
        add(viewMenu);
    }
}
