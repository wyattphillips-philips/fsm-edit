import javax.swing.*;

public class GraphEditor {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FSM Graph Editor");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GraphPanel panel = new GraphPanel();
            Node a = new Node(100, 100, 30, "A");
            Node b = new Node(250, 100, 30, "B");
            panel.addNode(a);
            panel.addNode(b);
            panel.addEdge(new Edge(a, b));

            frame.add(panel);
            frame.setSize(400, 300);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
