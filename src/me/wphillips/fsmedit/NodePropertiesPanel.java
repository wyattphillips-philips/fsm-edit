package me.wphillips.fsmedit;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class NodePropertiesPanel extends JPanel {
    private final JLabel labelLabel;
    private final JTextField labelField;
    private final JLabel xLabel;
    private final JSpinner xSpinner;
    private final JLabel yLabel;
    private final JSpinner ySpinner;
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
        add(labelField, gbc);

        // X position
        gbc.gridy++;
        gbc.weightx = 0;
        xLabel = new JLabel("X:");
        add(xLabel, gbc);
        gbc.gridy++;
        xSpinner = new JSpinner(new SpinnerNumberModel(0, -10000, 10000, 1));
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
        gbc.weightx = 1.0;
        add(xSpinner, gbc);

        // Y position
        gbc.gridy++;
        gbc.weightx = 0;
        yLabel = new JLabel("Y:");
        add(yLabel, gbc);
        gbc.gridy++;
        ySpinner = new JSpinner(new SpinnerNumberModel(0, -10000, 10000, 1));
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
        gbc.weightx = 1.0;
        add(ySpinner, gbc);

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
        metadataArea = new JTextArea(5, 10);
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
        setNode(null);
    }

    public void setNode(Node node) {
        this.node = node;
        boolean visible = node != null;
        labelLabel.setVisible(visible);
        labelField.setVisible(visible);
        xLabel.setVisible(visible);
        xSpinner.setVisible(visible);
        yLabel.setVisible(visible);
        ySpinner.setVisible(visible);
        colorLabel.setVisible(visible);
        colorButton.setVisible(visible);
        metadataLabel.setVisible(visible);
        metadataScroll.setVisible(visible);
        labelField.setEnabled(visible);
        xSpinner.setEnabled(visible);
        ySpinner.setEnabled(visible);
        colorButton.setEnabled(visible);
        metadataArea.setEnabled(visible);
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
}
