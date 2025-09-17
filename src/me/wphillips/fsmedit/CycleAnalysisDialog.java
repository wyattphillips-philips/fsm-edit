package me.wphillips.fsmedit;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.List;

/**
 * Dialog that guides the user through analyzing cycles starting from a chosen node.
 */
public class CycleAnalysisDialog extends JDialog {
    private final GraphPanel panel;
    private final JComboBox<Node> nodeSelector;
    private final JLabel statusLabel;
    private final JButton prevButton;
    private final JButton nextButton;
    private final JButton analyzeButton;
    private final JButton clearButton;
    private boolean hasRunAnalysis;

    public CycleAnalysisDialog(Window owner, GraphPanel panel) {
        super(owner, "Cycle Analysis", ModalityType.MODELESS);
        this.panel = panel;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        DefaultComboBoxModel<Node> nodeModel = new DefaultComboBoxModel<>();
        nodeSelector = new JComboBox<>(nodeModel);
        nodeSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                Component comp = super.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);
                if (value instanceof Node) {
                    Node node = (Node) value;
                    String label = node.getLabel();
                    if (label == null || label.isEmpty()) {
                        label = "(unnamed)";
                    }
                    setText(label);
                }
                return comp;
            }
        });

        analyzeButton = new JButton("Analyze");
        analyzeButton.addActionListener(e -> runAnalysis());

        prevButton = new JButton("\u2190");
        prevButton.addActionListener(e -> showPreviousCycle());

        nextButton = new JButton("\u2192");
        nextButton.addActionListener(e -> showNextCycle());

        statusLabel = new JLabel("Select a node and click Analyze to search for loops.");

        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearAnalysis());

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> setVisible(false));

        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectPanel.add(new JLabel("Start node:"));
        selectPanel.add(nodeSelector);
        selectPanel.add(analyzeButton);

        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        navigationPanel.add(prevButton);
        navigationPanel.add(statusLabel);
        navigationPanel.add(nextButton);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.add(clearButton);
        actionPanel.add(closeButton);

        setLayout(new BorderLayout(10, 10));
        add(selectPanel, BorderLayout.NORTH);
        add(navigationPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
        setResizable(false);

        refreshNodeList();
        updateNavigation();
    }

    private void refreshNodeList() {
        DefaultComboBoxModel<Node> model = (DefaultComboBoxModel<Node>) nodeSelector.getModel();
        model.removeAllElements();
        for (Node node : panel.getNodes()) {
            model.addElement(node);
        }
        boolean hasNodes = model.getSize() > 0;
        analyzeButton.setEnabled(hasNodes);
    }

    private void runAnalysis() {
        Node selected = (Node) nodeSelector.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a node to analyze.",
                    "No Node Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        hasRunAnalysis = true;
        List<List<Edge>> results = panel.findCyclesFrom(selected);
        if (results.isEmpty()) {
            panel.clearCycleAnalysis();
        } else {
            panel.setCycleAnalysis(results);
        }
        updateNavigation();
    }

    private void showPreviousCycle() {
        int count = panel.getCycleAnalysisCount();
        if (count == 0) {
            return;
        }
        int index = panel.getCurrentCycleIndex() - 1;
        if (index < 0) {
            index = count - 1;
        }
        panel.setCurrentCycleIndex(index);
        updateNavigation();
    }

    private void showNextCycle() {
        int count = panel.getCycleAnalysisCount();
        if (count == 0) {
            return;
        }
        int index = panel.getCurrentCycleIndex() + 1;
        if (index >= count) {
            index = 0;
        }
        panel.setCurrentCycleIndex(index);
        updateNavigation();
    }

    private void clearAnalysis() {
        panel.clearCycleAnalysis();
        hasRunAnalysis = false;
        updateNavigation();
    }

    private void updateNavigation() {
        DefaultComboBoxModel<Node> model = (DefaultComboBoxModel<Node>) nodeSelector.getModel();
        boolean hasNodes = model.getSize() > 0;
        analyzeButton.setEnabled(hasNodes);
        if (!hasNodes) {
            prevButton.setEnabled(false);
            nextButton.setEnabled(false);
            clearButton.setEnabled(hasRunAnalysis);
            statusLabel.setText("Add nodes to analyze cycles.");
            return;
        }

        int count = panel.getCycleAnalysisCount();
        boolean hasCycles = count > 0;
        prevButton.setEnabled(hasCycles && count > 1);
        nextButton.setEnabled(hasCycles && count > 1);
        clearButton.setEnabled(hasCycles || hasRunAnalysis);
        if (hasCycles) {
            int index = panel.getCurrentCycleIndex();
            if (index < 0) {
                index = 0;
            }
            statusLabel.setText(String.format("Loop %d of %d", index + 1, count));
        } else if (hasRunAnalysis) {
            statusLabel.setText("No loops found.");
        } else {
            statusLabel.setText("Select a node and click Analyze to search for loops.");
        }
    }
}
