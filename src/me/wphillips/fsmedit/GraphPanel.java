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
    private final GraphPopupMenu popupMenu;

    public GraphPanel() {
        popupMenu = new GraphPopupMenu(this);

        MouseAdapter handler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    for (Node n : nodes) {
                        if (n.contains(e.getX(), e.getY())) {
                            draggedNode = n;
                            lastMouseX = e.getX();
                            lastMouseY = e.getY();
                            break;
                        }
                    }
                } else if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    popupMenu.showMenu(GraphPanel.this, e.getX(), e.getY());
                    return;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.showMenu(GraphPanel.this, e.getX(), e.getY());
                }
                draggedNode = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedNode != null) {
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

    private Point boundaryPoint(Node from, Node to) {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);
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
