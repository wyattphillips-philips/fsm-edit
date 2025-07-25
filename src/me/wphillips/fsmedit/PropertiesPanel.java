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

public class PropertiesPanel extends JPanel {
    private final JLabel multiSelectLabel;
    private final JLabel labelLabel;
    private final JTextField labelField;
    private final JLabel idLabel;
    private final JLabel idValue;
    private final JLabel xLabel;
    private final JSpinner xSpinner;
    private final JLabel yLabel;
    private final JSpinner ySpinner;
    private final JPanel positionPanel;
    private final JLabel splineLabel;
    private final JComboBox<Edge.SplineType> splineCombo;
    private final JLabel curvatureLabel;
    private final JSpinner curvatureSpinner;
    private final JLabel edgeTextLabel;
    private final JTextField edgeTextField;
    private final JCheckBox lockPositionCheck;
    private final JLabel colorLabel;
    private final JButton colorButton;
    /** Reusable chooser to preserve custom swatches between uses. */
    private final JColorChooser colorChooser;
    /** Dialog wrapping {@link #colorChooser}. Recreated only once. */
    private JDialog colorDialog;
    private final JLabel metadataLabel;
    private final JTextArea metadataArea;
    private final JScrollPane metadataScroll;
    private final GraphPanel graphPanel;
    private Node node;
    private Edge edge;

    private final javax.swing.border.TitledBorder titledBorder;

    public PropertiesPanel(GraphPanel graphPanel) {
        this.graphPanel = graphPanel;
        setLayout(new GridBagLayout());
        titledBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Node Properties");
        setBorder(new CompoundBorder(titledBorder, new EmptyBorder(4, 4, 4, 4)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        multiSelectLabel = new JLabel();
        add(multiSelectLabel, gbc);
        multiSelectLabel.setVisible(false);
        titledBorder.setTitle("Node Properties");
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

        // Edge spline type
        gbc.gridy++;
        splineLabel = new JLabel("Spline:");
        add(splineLabel, gbc);
        gbc.gridy++;
        splineCombo = new JComboBox<>(Edge.SplineType.values());
        splineCombo.setEnabled(false);
        splineCombo.addActionListener(e -> {
            if (edge != null) {
                edge.setSplineType((Edge.SplineType) splineCombo.getSelectedItem());
                graphPanel.repaint();
            }
        });
        add(splineCombo, gbc);

        // Edge curvature
        gbc.gridy++;
        curvatureLabel = new JLabel("Curvature:");
        add(curvatureLabel, gbc);
        gbc.gridy++;
        curvatureSpinner = new JSpinner(new SpinnerNumberModel(0.4, -1.0, 1.0, 0.1));
        curvatureSpinner.setEnabled(false);
        curvatureSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (edge != null) {
                    edge.setCurvature(((Number) curvatureSpinner.getValue()).floatValue());
                    graphPanel.repaint();
                }
            }
        });
        add(curvatureSpinner, gbc);

        // Edge text
        gbc.gridy++;
        edgeTextLabel = new JLabel("Text:");
        add(edgeTextLabel, gbc);
        gbc.gridy++;
        edgeTextField = new JTextField();
        edgeTextField.setEnabled(false);
        edgeTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
            private void update() {
                if (edge != null) {
                    edge.setText(edgeTextField.getText());
                    graphPanel.repaint();
                }
            }
        });
        add(edgeTextField, gbc);

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
        colorChooser = new JColorChooser();
        colorDialog = null;
        colorButton.addActionListener(e -> {
            if (node != null) {
                if (colorDialog == null) {
                    colorDialog = JColorChooser.createDialog(PropertiesPanel.this,
                            "Choose Node Color", true, colorChooser, event -> {
                                if (node != null) {
                                    Color chosen = colorChooser.getColor();
                                    node.setColor(chosen);
                                    colorButton.setBackground(chosen);
                                    graphPanel.repaint();
                                }
                            }, null);
                }
                colorChooser.setColor(node.getColor());
                colorDialog.setVisible(true);
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
        // Show enough space for at least four lines of text
        metadataArea = new JTextArea();
        metadataArea.setRows(6);
        metadataArea.setColumns(10);
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
        Dimension metaSize = metadataArea.getPreferredScrollableViewportSize();
        metadataScroll.setPreferredSize(metaSize);
        metadataScroll.setMinimumSize(metaSize);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(metadataScroll, gbc);

        // Fill remaining space so the UUID is pinned to the bottom
        gbc.gridy++;
        gbc.weighty = 1;
        add(Box.createVerticalGlue(), gbc);

        // UUID displayed at the bottom of the panel
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        idLabel = new JLabel("UUID:");
        add(idLabel, gbc);
        gbc.gridy++;
        gbc.weightx = 1.0;
        idValue = new JLabel();
        add(idValue, gbc);
        // Wider panel so the UUID fits without wrapping
        setPreferredSize(new Dimension(300, 0));

        // Start with no node selected so the fields are hidden initially
        setNodes(java.util.Collections.emptyList());
    }

    public void setNode(Node node) {
        multiSelectLabel.setVisible(false);
        if (this.node != null && this.node != node) {
            commitPositionEdits();
        }
        this.node = node;
        this.edge = null;
        titledBorder.setTitle("Node Properties");
        boolean visible = node != null;
        labelLabel.setVisible(visible);
        labelField.setVisible(visible);
        idLabel.setVisible(visible);
        idValue.setVisible(visible);
        positionPanel.setVisible(visible);
        splineLabel.setVisible(false);
        splineCombo.setVisible(false);
        curvatureLabel.setVisible(false);
        curvatureSpinner.setVisible(false);
        edgeTextLabel.setVisible(false);
        edgeTextField.setVisible(false);
        colorLabel.setVisible(visible);
        colorButton.setVisible(visible);
        metadataLabel.setVisible(visible);
        metadataScroll.setVisible(visible);
        lockPositionCheck.setVisible(visible);
        lockPositionCheck.setEnabled(visible);
        labelField.setEnabled(visible);
        colorButton.setEnabled(visible);
        metadataArea.setEnabled(visible);
        edgeTextField.setEnabled(false);
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
            idValue.setText("");
            xSpinner.setValue(0);
            ySpinner.setValue(0);
            colorButton.setBackground(null);
            metadataArea.setText("");
        } else {
            labelField.setText(node.getLabel());
            idValue.setText(node.getId());
            xSpinner.setValue(node.getX());
            ySpinner.setValue(node.getY());
            colorButton.setBackground(node.getColor());
            metadataArea.setText(node.getMetadata());
        }
        revalidate();
        repaint();
    }

    public void setEdge(Edge edge) {
        if (this.node != null) {
            commitPositionEdits();
        }
        this.node = null;
        this.edge = edge;
        titledBorder.setTitle("Edge Properties");
        boolean visible = edge != null;
        multiSelectLabel.setVisible(false);
        labelLabel.setVisible(false);
        labelField.setVisible(false);
        idLabel.setVisible(visible);
        idValue.setVisible(visible);
        positionPanel.setVisible(false);
        lockPositionCheck.setVisible(false);
        colorLabel.setVisible(false);
        colorButton.setVisible(false);
        metadataLabel.setVisible(false);
        metadataScroll.setVisible(false);
        splineLabel.setVisible(visible);
        splineCombo.setVisible(visible);
        curvatureLabel.setVisible(visible);
        curvatureSpinner.setVisible(visible);
        edgeTextLabel.setVisible(visible);
        edgeTextField.setVisible(visible);
        splineCombo.setEnabled(visible);
        curvatureSpinner.setEnabled(visible);
        edgeTextField.setEnabled(visible);
        if (edge == null) {
            splineCombo.setSelectedIndex(0);
            curvatureSpinner.setValue(0.4);
            edgeTextField.setText("");
            idValue.setText("");
        } else {
            splineCombo.setSelectedItem(edge.getSplineType());
            curvatureSpinner.setValue((double) edge.getCurvature());
            edgeTextField.setText(edge.getText());
            idValue.setText(edge.getId());
        }
        revalidate();
        repaint();
    }

    public void setNodes(java.util.List<Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            if (this.node != null) {
                commitPositionEdits();
            }
            this.node = null;
            this.edge = null;
            titledBorder.setTitle("Select an element");
            multiSelectLabel.setVisible(false);
            labelLabel.setVisible(false);
            labelField.setVisible(false);
            idLabel.setVisible(false);
            idValue.setVisible(false);
            positionPanel.setVisible(false);
            splineLabel.setVisible(false);
            splineCombo.setVisible(false);
            curvatureLabel.setVisible(false);
            curvatureSpinner.setVisible(false);
            edgeTextLabel.setVisible(false);
            edgeTextField.setVisible(false);
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
        this.edge = null;
        titledBorder.setTitle("Properties");
        multiSelectLabel.setText(nodes.size() + " Nodes Selected");
        multiSelectLabel.setVisible(true);
        labelLabel.setVisible(false);
        labelField.setVisible(false);
        idLabel.setVisible(false);
        idValue.setVisible(false);
        positionPanel.setVisible(false);
        splineLabel.setVisible(false);
        splineCombo.setVisible(false);
        curvatureLabel.setVisible(false);
        curvatureSpinner.setVisible(false);
        edgeTextLabel.setVisible(false);
        edgeTextField.setVisible(false);
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
