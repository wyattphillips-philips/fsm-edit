package me.wphillips.fsmedit;

import javax.swing.*;

public class GraphEditor {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FSM Graph Editor");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JMenuBar menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu("File");
            JMenuItem exitItem = new JMenuItem("Exit");
            exitItem.addActionListener(e -> System.exit(0));
            fileMenu.add(exitItem);
            JMenu editMenu = new JMenu("Edit");
            menuBar.add(fileMenu);
            menuBar.add(editMenu);
            frame.setJMenuBar(menuBar);

            GraphPanel panel = new GraphPanel();
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

            frame.add(panel);
            frame.setSize(400, 300);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
