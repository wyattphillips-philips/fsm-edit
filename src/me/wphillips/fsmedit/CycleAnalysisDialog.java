package me.wphillips.fsmedit;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Dialog that guides the user through analyzing cycles starting from a chosen node.
 */
public class CycleAnalysisDialog extends JDialog {
    private final GraphPanel panel;
    private Node selectedStartNode;
    private final JLabel startNodeLabel;
    private final JLabel statusLabel;
    private final JButton prevButton;
    private final JButton nextButton;
    private final JButton analyzeButton;
    private final JButton copyButton;
    private final JButton clearButton;
    private final JTextArea loopDetailsArea;
    private boolean hasRunAnalysis;
    private final GraphPanel.NodeSelectionListener nodeSelectionListener;

    public CycleAnalysisDialog(Window owner, GraphPanel panel) {
        super(owner, "Cycle Analysis", ModalityType.MODELESS);
        this.panel = panel;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        analyzeButton = new JButton("Analyze");
        analyzeButton.addActionListener(e -> runAnalysis());
        analyzeButton.setEnabled(false);

        prevButton = new JButton("\u2190");
        prevButton.addActionListener(e -> showPreviousCycle());

        nextButton = new JButton("\u2192");
        nextButton.addActionListener(e -> showNextCycle());

        statusLabel = new JLabel("Click a node in the graph to choose a start point.");

        startNodeLabel = new JLabel("(click a node in the graph)");

        loopDetailsArea = new JTextArea(3, 30);
        loopDetailsArea.setLineWrap(true);
        loopDetailsArea.setWrapStyleWord(true);
        loopDetailsArea.setEditable(false);
        loopDetailsArea.setFocusable(false);
        loopDetailsArea.setMargin(new Insets(6, 6, 6, 6));
        Color inactive = UIManager.getColor("TextArea.inactiveBackground");
        if (inactive != null) {
            loopDetailsArea.setBackground(inactive);
        }
        loopDetailsArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> copyLoopDetails());
        copyButton.setEnabled(false);

        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearAnalysis());

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectPanel.add(new JLabel("Start node:"));
        selectPanel.add(startNodeLabel);
        selectPanel.add(analyzeButton);

        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        navigationPanel.add(prevButton);
        navigationPanel.add(statusLabel);
        navigationPanel.add(nextButton);

        JPanel loopPanel = new JPanel(new BorderLayout());
        loopPanel.setBorder(BorderFactory.createTitledBorder("Loop Path"));
        JScrollPane loopScroll = new JScrollPane(loopDetailsArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        loopScroll.setBorder(BorderFactory.createEmptyBorder());
        loopPanel.add(loopScroll, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.add(copyButton);
        actionPanel.add(clearButton);
        actionPanel.add(closeButton);

        setLayout(new BorderLayout(10, 10));
        add(selectPanel, BorderLayout.NORTH);
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(loopPanel, BorderLayout.CENTER);
        centerPanel.add(navigationPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);

        List<Node> selected = panel.getSelectedNodes();
        if (!selected.isEmpty()) {
            selectedStartNode = selected.get(0);
        }
        updateStartNodeLabel(selectedStartNode);
        updateNavigation();

        nodeSelectionListener = this::handleNodeSelection;
        panel.addNodeSelectionListener(nodeSelectionListener);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                panel.removeNodeSelectionListener(nodeSelectionListener);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                panel.removeNodeSelectionListener(nodeSelectionListener);
            }
        });

        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void handleNodeSelection(Node node) {
        if (node == null) {
            return;
        }
        if (node != selectedStartNode) {
            selectedStartNode = node;
            hasRunAnalysis = false;
            panel.clearCycleAnalysis();
        }
        updateStartNodeLabel(selectedStartNode);
        updateNavigation();
    }

    private void updateStartNodeLabel(Node node) {
        if (node == null) {
            startNodeLabel.setText("(click a node in the graph)");
        } else {
            startNodeLabel.setText(formatNodeLabel(node));
        }
    }

    private void runAnalysis() {
        if (selectedStartNode == null) {
            JOptionPane.showMessageDialog(this,
                    "Click a node in the graph to choose a start point.",
                    "No Node Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        hasRunAnalysis = true;
        List<List<Edge>> results = panel.findCyclesFrom(selectedStartNode);
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
        if (selectedStartNode != null && !panel.getNodes().contains(selectedStartNode)) {
            selectedStartNode = null;
            updateStartNodeLabel(null);
        }

        boolean hasNodes = panel.getNodeCount() > 0;
        boolean hasStartNode = selectedStartNode != null;
        analyzeButton.setEnabled(hasNodes && hasStartNode);
        if (!hasNodes) {
            prevButton.setEnabled(false);
            nextButton.setEnabled(false);
            clearButton.setEnabled(hasRunAnalysis);
            statusLabel.setText("Add nodes to analyze cycles.");
            loopDetailsArea.setText("");
            updateCopyButtonState();
            return;
        }

        if (!hasStartNode) {
            prevButton.setEnabled(false);
            nextButton.setEnabled(false);
            clearButton.setEnabled(hasRunAnalysis);
            statusLabel.setText("Click a node in the graph to choose a start point.");
            loopDetailsArea.setText("");
            updateCopyButtonState();
            return;
        }

        int count = panel.getCycleAnalysisCount();
        boolean hasCycles = count > 0;
        prevButton.setEnabled(hasCycles && count > 1);
        nextButton.setEnabled(hasCycles && count > 1);
        clearButton.setEnabled(hasCycles || hasRunAnalysis);
        if (hasCycles) {
            int index = panel.getCurrentCycleIndex();
            if (index < 0 || index >= count) {
                index = 0;
            }
            statusLabel.setText(String.format("Loop %d of %d", index + 1, count));
            loopDetailsArea.setText(describeCycle(panel.getCycleAnalysisLoops().get(index)));
        } else if (hasRunAnalysis) {
            statusLabel.setText("No loops found.");
            loopDetailsArea.setText("");
        } else {
            statusLabel.setText("Click Analyze to search for loops.");
            loopDetailsArea.setText("");
        }
        loopDetailsArea.setCaretPosition(0);
        updateCopyButtonState();
    }

    private void copyLoopDetails() {
        String text = loopDetailsArea.getText();
        if (text == null || text.isEmpty()) {
            return;
        }
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(text), null);
    }

    private void updateCopyButtonState() {
        String text = loopDetailsArea.getText();
        copyButton.setEnabled(text != null && !text.isEmpty());
    }

    private String describeCycle(List<Edge> cycle) {
        if (cycle == null || cycle.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        Node start = cycle.get(0).getFrom();
        builder.append(formatNodeLabel(start));
        for (Edge edge : cycle) {
            builder.append(" -> ");
            builder.append(formatNodeLabel(edge.getTo()));
        }
        return builder.toString();
    }

    private String formatNodeLabel(Node node) {
        if (node == null) {
            return "";
        }
        String label = node.getLabel();
        if (label == null || label.isEmpty()) {
            label = "(unnamed)";
        }
        return label;
    }
}
