package me.wphillips.fsmedit;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class NodePropertiesPanel extends JPanel {
    private final JLabel labelLabel;
    private final JTextField labelField;
    private final JLabel colorLabel;
    private final JButton colorButton;
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
        gbc.weighty = 1;
        gbc.gridy++;
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
        colorLabel.setVisible(visible);
        colorButton.setVisible(visible);
        labelField.setEnabled(visible);
        colorButton.setEnabled(visible);
        if (node == null) {
            labelField.setText("");
            colorButton.setBackground(null);
        } else {
            labelField.setText(node.getLabel());
            colorButton.setBackground(node.getColor());
        }
        revalidate();
        repaint();
    }
}
