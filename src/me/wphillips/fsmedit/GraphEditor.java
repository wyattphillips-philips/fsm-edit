package me.wphillips.fsmedit;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.List;

public class GraphEditor {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FSM Graph Editor");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GraphPanel panel = new GraphPanel();
            frame.setJMenuBar(new GraphMenuBar(panel));
            PropertiesPanel propertiesPanel = new PropertiesPanel(panel);
            panel.setPropertiesPanel(propertiesPanel);
            frame.setLayout(new BorderLayout());
            if (args.length > 0) {
                try {
                    File file = GraphIO.withExtension(new File(args[0]));
                    panel.loadGraph(file);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Failed to open file: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            frame.add(panel, BorderLayout.CENTER);
            frame.add(propertiesPanel, BorderLayout.EAST);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            panel.setTransferHandler(new TransferHandler() {
                @Override
                public boolean canImport(TransferHandler.TransferSupport support) {
                    return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
                }

                @SuppressWarnings("unchecked")
                @Override
                public boolean importData(TransferHandler.TransferSupport support) {
                    if (!canImport(support)) {
                        return false;
                    }
                    try {
                        List<File> files = (List<File>) support.getTransferable()
                                .getTransferData(DataFlavor.javaFileListFlavor);
                        if (!files.isEmpty()) {
                            panel.loadGraph(GraphIO.withExtension(files.get(0)));
                            return true;
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame,
                                "Failed to open file: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return false;
                }
            });

            frame.setVisible(true);
        });
    }
}
