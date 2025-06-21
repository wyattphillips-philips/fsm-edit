package me.wphillips.fsmedit;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class NodePropertiesPanel extends JPanel {
    private final JLabel multiSelectLabel;
    private final JLabel labelLabel;
    private final JTextField labelField;
    private final JLabel xLabel;
    private final JSpinner xSpinner;
    private final JLabel yLabel;
    private final JSpinner ySpinner;
    private final JPanel positionPanel;
    private final JCheckBox lockPositionCheck;
    private final JLabel colorLabel;
    private final JButton colorButton;
    private final JLabel metadataLabel;
    private final JTextArea metadataArea;
    private final JScrollPane metadataScroll;
    private final GraphPanel graphPanel;
    private Node node;

    public NodePropertiesPanel(GraphPanel graphPanel) {
        this.graphPanel = graphPanel;
        setLayout(new GridBagLayout());
        setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Node Properties"),
                new EmptyBorder(4, 4, 4, 4)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        multiSelectLabel = new JLabel("Multiple Items Selected");
        add(multiSelectLabel, gbc);
        multiSelectLabel.setVisible(false);
        gbc.gridy++;
        gbc.weightx = 0;
        labelLabel = new JLabel("Label:");
        add(labelLabel, gbc);
        gbc.gridy++;
        labelField = new JTextField();
        labelField.setEnabled(false);
        labelField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
            private void update() {
                if (node != null) {
                    node.setLabel(labelField.getText());
                    graphPanel.repaint();
                }
            }
        });
        gbc.weightx = 1.0;
        // Add a bit more bottom margin below the label field
        gbc.insets = new Insets(2, 2, 6, 2);
        add(labelField, gbc);
        // Restore default insets for subsequent rows
        gbc.insets = new Insets(2, 2, 2, 2);

        // Position panel with X and Y side by side
        gbc.gridy++;
        gbc.weightx = 1.0;
        positionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));

        xLabel = new JLabel("X:");
        positionPanel.add(xLabel);
        xSpinner = new JSpinner(new SpinnerNumberModel(0, -10000, 10000, 1));
        xSpinner.setPreferredSize(new Dimension(60, xSpinner.getPreferredSize().height));
        xSpinner.setEnabled(false);
        xSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (node != null) {
                    node.setX((Integer) xSpinner.getValue());
                    graphPanel.repaint();
                }
            }
        });
        // Use standard spinner behavior without committing on every keystroke
        positionPanel.add(xSpinner);

        yLabel = new JLabel("Y:");
        positionPanel.add(yLabel);
        ySpinner = new JSpinner(new SpinnerNumberModel(0, -10000, 10000, 1));
        ySpinner.setPreferredSize(new Dimension(60, ySpinner.getPreferredSize().height));
        ySpinner.setEnabled(false);
        ySpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (node != null) {
                    node.setY((Integer) ySpinner.getValue());
                    graphPanel.repaint();
                }
            }
        });
        // Use standard spinner behavior without committing on every keystroke
        positionPanel.add(ySpinner);

        add(positionPanel, gbc);

        // Lock checkbox just below the position controls
        gbc.gridy++;
        lockPositionCheck = new JCheckBox("Lock Position");
        lockPositionCheck.setEnabled(false);
        lockPositionCheck.addActionListener(e -> {
            if (node != null) {
                node.setLocked(lockPositionCheck.isSelected());
                xSpinner.setEnabled(!node.isLocked());
                ySpinner.setEnabled(!node.isLocked());
            }
        });
        add(lockPositionCheck, gbc);

        // Color
        gbc.gridy++;
        gbc.weightx = 0;
        colorLabel = new JLabel("Color:");
        add(colorLabel, gbc);
        gbc.gridy++;
        colorButton = new JButton("Select Color");
        colorButton.setEnabled(false);
        colorButton.addActionListener(e -> {
            if (node != null) {
                Color newColor = JColorChooser.showDialog(NodePropertiesPanel.this,
                        "Choose Node Color", node.getColor());
                if (newColor != null) {
                    node.setColor(newColor);
                    colorButton.setBackground(newColor);
                    graphPanel.repaint();
                }
            }
        });
        gbc.weightx = 1.0;
        add(colorButton, gbc);

        // Metadata text area
        gbc.gridy++;
        gbc.weightx = 0;
        metadataLabel = new JLabel("Metadata:");
        add(metadataLabel, gbc);
        gbc.gridy++;
        // Slightly taller area for notes
        metadataArea = new JTextArea(8, 10);
        metadataArea.setLineWrap(true);
        metadataArea.setWrapStyleWord(true);
        metadataArea.setEnabled(false);
        metadataArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
            private void update() {
                if (node != null) {
                    node.setMetadata(metadataArea.getText());
                }
            }
        });
        metadataScroll = new JScrollPane(metadataArea);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(metadataScroll, gbc);

        gbc.weighty = 1;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(Box.createVerticalGlue(), gbc);
        setPreferredSize(new Dimension(180, 0));

        // Start with no node selected so the fields are hidden initially
        setNodes(java.util.Collections.emptyList());
    }

    public void setNode(Node node) {
        multiSelectLabel.setVisible(false);
        if (this.node != null && this.node != node) {
            commitPositionEdits();
        }
        this.node = node;
        boolean visible = node != null;
        labelLabel.setVisible(visible);
        labelField.setVisible(visible);
        positionPanel.setVisible(visible);
        colorLabel.setVisible(visible);
        colorButton.setVisible(visible);
        metadataLabel.setVisible(visible);
        metadataScroll.setVisible(visible);
        lockPositionCheck.setVisible(visible);
        lockPositionCheck.setEnabled(visible);
        labelField.setEnabled(visible);
        colorButton.setEnabled(visible);
        metadataArea.setEnabled(visible);
        if (visible) {
            xSpinner.setEnabled(!node.isLocked());
            ySpinner.setEnabled(!node.isLocked());
            lockPositionCheck.setSelected(node.isLocked());
            lockPositionCheck.setEnabled(true);
        } else {
            xSpinner.setEnabled(false);
            ySpinner.setEnabled(false);
            lockPositionCheck.setSelected(false);
            lockPositionCheck.setEnabled(false);
        }
        if (node == null) {
            labelField.setText("");
            xSpinner.setValue(0);
            ySpinner.setValue(0);
            colorButton.setBackground(null);
            metadataArea.setText("");
        } else {
            labelField.setText(node.getLabel());
            xSpinner.setValue(node.getX());
            ySpinner.setValue(node.getY());
            colorButton.setBackground(node.getColor());
            metadataArea.setText(node.getMetadata());
        }
        revalidate();
        repaint();
    }

    public void setNodes(java.util.List<Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            setNode(null);
            return;
        }
        if (nodes.size() == 1) {
            setNode(nodes.get(0));
            return;
        }
        if (this.node != null) {
            commitPositionEdits();
        }
        this.node = null;
        multiSelectLabel.setVisible(true);
        labelLabel.setVisible(false);
        labelField.setVisible(false);
        positionPanel.setVisible(false);
        colorLabel.setVisible(false);
        colorButton.setVisible(false);
        metadataLabel.setVisible(false);
        metadataScroll.setVisible(false);
        lockPositionCheck.setVisible(false);
        lockPositionCheck.setEnabled(false);
        labelField.setEnabled(false);
        colorButton.setEnabled(false);
        metadataArea.setEnabled(false);
        revalidate();
        repaint();
    }

    /**
     * Refresh the X and Y spinner values from the current node state. This
     * is used by the graph panel when a node is dragged.
     */
    public void updatePositionFields() {
        if (node != null) {
            xSpinner.setValue(node.getX());
            ySpinner.setValue(node.getY());
            xSpinner.setEnabled(!node.isLocked());
            ySpinner.setEnabled(!node.isLocked());
            lockPositionCheck.setSelected(node.isLocked());
            lockPositionCheck.setEnabled(true);
        }
    }

    /**
     * Commit any unconfirmed edits in the X and Y spinners back to the node.
     */
    private void commitPositionEdits() {
        if (node != null && !node.isLocked()) {
            try {
                xSpinner.commitEdit();
                ySpinner.commitEdit();
            } catch (java.text.ParseException ignored) {
                // If the user entered invalid text, ignore and keep last value
            }
            node.setX((Integer) xSpinner.getValue());
            node.setY((Integer) ySpinner.getValue());
            graphPanel.repaint();
        }
    }
}
