package me.wphillips.fsmedit;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class GraphPanel extends JPanel {
    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private Node startNode;
    private Node draggedNode;
    private int lastMouseX;
    private int lastMouseY;
    private Node edgeStart;
    private Node tempEdgeNode;
    private final GraphPopupMenu popupMenu;

    public GraphPanel() {
        popupMenu = new GraphPopupMenu(this);

        MouseAdapter handler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Node hit = getNodeAt(e.getX(), e.getY());
                    if (hit != null) {
                        if (e.isControlDown()) {
                            edgeStart = hit;
                            tempEdgeNode = new Node(e.getX(), e.getY(), 0, "");
                            repaint();
                        } else {
                            draggedNode = hit;
                            lastMouseX = e.getX();
                            lastMouseY = e.getY();
                        }
                    }
                } else if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    Node hit = getNodeAt(e.getX(), e.getY());
                    popupMenu.showMenu(GraphPanel.this, e.getX(), e.getY(), hit);
                    return;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (edgeStart != null) {
                    Node hit = getNodeAt(e.getX(), e.getY());
                    if (hit != null && hit != edgeStart) {
                        addEdge(new Edge(edgeStart, hit));
                    }
                    edgeStart = null;
                    tempEdgeNode = null;
                    repaint();
                } else {
                    if (e.isPopupTrigger()) {
                        Node hit = getNodeAt(e.getX(), e.getY());
                        popupMenu.showMenu(GraphPanel.this, e.getX(), e.getY(), hit);
                    }
                    draggedNode = null;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (edgeStart != null) {
                    tempEdgeNode.setPosition(e.getX(), e.getY());
                    repaint();
                } else if (draggedNode != null) {
                    int dx = e.getX() - lastMouseX;
                    int dy = e.getY() - lastMouseY;
                    draggedNode.moveBy(dx, dy);
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                    repaint();
                }
            }
        };

        addMouseListener(handler);
        addMouseMotionListener(handler);
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    /**
     * Get the current number of nodes in the graph.
     */
    public int getNodeCount() {
        return nodes.size();
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    /**
     * Remove a node and any edges that reference it.
     */
    public void removeNode(Node node) {
        nodes.remove(node);
        edges.removeIf(e -> e.getFrom() == node || e.getTo() == node);
        if (startNode == node) {
            startNode = null;
        }
        repaint();
    }

    /**
     * Return the topmost node at the given coordinates, or {@code null}.
     */
    private Node getNodeAt(int x, int y) {
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node n = nodes.get(i);
            if (n.contains(x, y)) {
                return n;
            }
        }
        return null;
    }

    public void setStartNode(Node node) {
        this.startNode = node;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw edges first
        g2.setColor(Color.BLACK);
        for (Edge e : edges) {
            drawArrow(g2, e.getFrom(), e.getTo());
        }
        if (edgeStart != null && tempEdgeNode != null) {
            drawArrow(g2, edgeStart, new Point(tempEdgeNode.getX(), tempEdgeNode.getY()));
        }

        // Draw nodes on top
        for (Node n : nodes) {
            drawNode(g2, n);
        }
    }

    private void drawNode(Graphics2D g2, Node n) {
        int r = n.getRadius();
        int x = n.getX() - r;
        int y = n.getY() - r;
        if (n == startNode) {
            g2.setColor(new Color(144, 238, 144)); // light green
        } else {
            g2.setColor(Color.WHITE);
        }
        g2.fillOval(x, y, 2 * r, 2 * r);
        g2.setColor(Color.BLACK);
        g2.drawOval(x, y, 2 * r, 2 * r);
        // Draw label centered
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(n.getLabel());
        int textHeight = fm.getAscent();
        g2.drawString(n.getLabel(), n.getX() - textWidth / 2, n.getY() + textHeight / 2);
    }

    private void drawArrow(Graphics2D g2, Node from, Node to) {
        Point p1 = boundaryPoint(from, to);
        Point p2 = boundaryPoint(to, from);
        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        drawArrowHead(g2, p1, p2);
    }

    private void drawArrow(Graphics2D g2, Node from, Point to) {
        Point p1 = boundaryPoint(from, to.x, to.y);
        Point p2 = new Point(to.x, to.y);
        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        drawArrowHead(g2, p1, p2);
    }

    private Point boundaryPoint(Node from, Node to) {
        return boundaryPoint(from, to.getX(), to.getY());
    }

    private Point boundaryPoint(Node from, int toX, int toY) {
        double dx = toX - from.getX();
        double dy = toY - from.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) {
            return new Point(from.getX(), from.getY());
        }
        double ratio = from.getRadius() / dist;
        int x = (int) Math.round(from.getX() + dx * ratio);
        int y = (int) Math.round(from.getY() + dy * ratio);
        return new Point(x, y);
    }

    private void drawArrowHead(Graphics2D g2, Point from, Point to) {
        final int barb = 10;
        final double phi = Math.toRadians(40);
        double dy = to.y - from.y;
        double dx = to.x - from.x;
        double theta = Math.atan2(dy, dx);
        for (int j = 0; j < 2; j++) {
            double rho = theta + phi - j * 2 * phi;
            double x = to.x - barb * Math.cos(rho);
            double y = to.y - barb * Math.sin(rho);
            g2.draw(new Line2D.Double(to.x, to.y, x, y));
        }
    }
}
