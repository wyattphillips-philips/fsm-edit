package me.wphillips.fsmedit;

import javax.swing.*;
import java.awt.BorderLayout;
import me.wphillips.fsmedit.PropertiesPanel;

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
            Node a = new Node(100, 100, 30, "A");
            Node b = new Node(250, 100, 30, "B");
            Node c = new Node(175, 200, 30, "C");
            panel.addNode(a);
            panel.addNode(b);
            panel.addNode(c);
            panel.setStartNode(a);
            panel.addEdge(new Edge(a, b));
            panel.addEdge(new Edge(b, c));
            panel.addEdge(new Edge(c, a));

            frame.add(panel, BorderLayout.CENTER);
            frame.add(propertiesPanel, BorderLayout.EAST);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
