package me.wphillips.fsmedit;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Stroke;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import me.wphillips.fsmedit.NodePropertiesPanel;
import me.wphillips.fsmedit.GraphIO;

public class GraphPanel extends JPanel {
    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private Node startNode;
    private Node draggedNode;
    private Node hoveredNode;
    private Node selectedNode;
    private NodePropertiesPanel propertiesPanel;
    private int lastMouseX;
    private int lastMouseY;
    private Node edgeStart;
    private Node tempEdgeNode;
    private Node edgeTarget;
    private Edge editingEdge;
    private final GraphPopupMenu popupMenu;

    /**
     * Update which node is currently hovered and adjust the cursor. The panel
     * is repainted only when the hovered node actually changes.
     */
    private void setHoveredNode(Node node) {
        if (hoveredNode != node) {
            hoveredNode = node;
            if (node != null && !node.isLocked()) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else if (node != null) {
                setCursor(Cursor.getDefaultCursor());
            } else {
                setCursor(Cursor.getDefaultCursor());
            }
            repaint();
        }
    }

    public GraphPanel() {
        popupMenu = new GraphPopupMenu(this);

        MouseAdapter handler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.isControlDown()) {
                        Edge edgeHit = getEdgeAtArrowHead(e.getX(), e.getY());
                        if (edgeHit != null) {
                            editingEdge = edgeHit;
                            edgeStart = editingEdge.getFrom();
                            tempEdgeNode = new Node(e.getX(), e.getY(), 0, "");
                            edgeTarget = null;
                            repaint();
                            return;
                        }
                    }
                    Node hit = getNodeAt(e.getX(), e.getY());
                    if (hit != null) {
                        if (e.isControlDown()) {
                            edgeStart = hit;
                            tempEdgeNode = new Node(e.getX(), e.getY(), 0, "");
                            edgeTarget = null;
                            repaint();
                        } else {
                            selectedNode = hit;
                            if (propertiesPanel != null) {
                                propertiesPanel.setNode(hit);
                            }
                            if (!hit.isLocked()) {
                                draggedNode = hit;
                                lastMouseX = e.getX();
                                lastMouseY = e.getY();
                            } else {
                                draggedNode = null;
                            }
                            repaint();
                        }
                    } else {
                        selectedNode = null;
                        if (propertiesPanel != null) {
                            propertiesPanel.setNode(null);
                        }
                        repaint();
                    }
                } else if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    Node hit = getNodeAt(e.getX(), e.getY());
                    popupMenu.showMenu(GraphPanel.this, e.getX(), e.getY(), hit);
                    return;
                }
                setHoveredNode(getNodeAt(e.getX(), e.getY()));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (editingEdge != null) {
                    Node hit = getNodeAt(e.getX(), e.getY());
                    if (hit != null && hit != edgeStart) {
                        editingEdge.setTo(hit);
                    } else {
                        edges.remove(editingEdge);
                    }
                    editingEdge = null;
                    edgeStart = null;
                    tempEdgeNode = null;
                    edgeTarget = null;
                    repaint();
                } else if (edgeStart != null) {
                    Node hit = getNodeAt(e.getX(), e.getY());
                    if (hit != null && hit != edgeStart) {
                        addEdge(new Edge(edgeStart, hit));
                    }
                    edgeStart = null;
                    tempEdgeNode = null;
                    edgeTarget = null;
                    repaint();
                } else {
                    if (e.isPopupTrigger()) {
                        Node hit = getNodeAt(e.getX(), e.getY());
                        popupMenu.showMenu(GraphPanel.this, e.getX(), e.getY(), hit);
                    }
                    draggedNode = null;
                }
                setHoveredNode(getNodeAt(e.getX(), e.getY()));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (editingEdge != null) {
                    tempEdgeNode.setPosition(e.getX(), e.getY());
                    Node hit = getNodeAt(e.getX(), e.getY());
                    if (hit != null && hit != edgeStart) {
                        edgeTarget = hit;
                    } else {
                        edgeTarget = null;
                    }
                    repaint();
                } else if (edgeStart != null) {
                    tempEdgeNode.setPosition(e.getX(), e.getY());
                    Node hit = getNodeAt(e.getX(), e.getY());
                    if (hit != null && hit != edgeStart) {
                        edgeTarget = hit;
                    } else {
                        edgeTarget = null;
                    }
                    repaint();
                } else if (draggedNode != null) {
                    if (!draggedNode.isLocked()) {
                        int dx = e.getX() - lastMouseX;
                        int dy = e.getY() - lastMouseY;
                        draggedNode.moveBy(dx, dy);
                        lastMouseX = e.getX();
                        lastMouseY = e.getY();
                        if (propertiesPanel != null) {
                            propertiesPanel.updatePositionFields();
                        }
                        repaint();
                    } else {
                        draggedNode = null;
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                setHoveredNode(getNodeAt(e.getX(), e.getY()));
            }
        };

        addMouseListener(handler);
        addMouseMotionListener(handler);
    }

    public void setPropertiesPanel(NodePropertiesPanel panel) {
        this.propertiesPanel = panel;
        if (panel != null) {
            panel.setNode(selectedNode);
        }
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
     * Get the number of edges in the graph.
     */
    public int getEdgeCount() {
        return edges.size();
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
        if (selectedNode == node) {
            selectedNode = null;
            if (propertiesPanel != null) {
                propertiesPanel.setNode(null);
            }
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

    /**
     * Return the topmost edge whose arrow head is near the given coordinates.
     */
    private Edge getEdgeAtArrowHead(int x, int y) {
        final int threshold = 10;
        for (int i = edges.size() - 1; i >= 0; i--) {
            Edge e = edges.get(i);
            Point tip = boundaryPoint(e.getTo(), e.getFrom());
            int dx = x - tip.x;
            int dy = y - tip.y;
            if (dx * dx + dy * dy <= threshold * threshold) {
                return e;
            }
        }
        return null;
    }

    public void setStartNode(Node node) {
        this.startNode = node;
        repaint();
    }

    /**
     * Remove all nodes and edges from the graph.
     */
    public void clearGraph() {
        nodes.clear();
        edges.clear();
        startNode = null;
        selectedNode = null;
        hoveredNode = null;
        draggedNode = null;
        editingEdge = null;
        edgeStart = null;
        tempEdgeNode = null;
        edgeTarget = null;
        if (propertiesPanel != null) {
            propertiesPanel.setNode(null);
        }
        repaint();
    }

    /**
     * Serialize the current graph to the specified file.
     */
    public void saveGraph(File file) throws IOException {
        GraphIO.save(GraphIO.withExtension(file),
                new GraphModel(nodes, edges, startNode));
    }

    /**
     * Load a graph from the given file, replacing the current contents.
     */
    public void loadGraph(File file) throws IOException, ClassNotFoundException {
        GraphModel model = GraphIO.load(GraphIO.withExtension(file));
        nodes.clear();
        nodes.addAll(model.getNodes());
        edges.clear();
        edges.addAll(model.getEdges());
        startNode = model.getStartNode();
        selectedNode = null;
        hoveredNode = null;
        draggedNode = null;
        editingEdge = null;
        edgeStart = null;
        tempEdgeNode = null;
        edgeTarget = null;
        if (propertiesPanel != null) {
            propertiesPanel.setNode(null);
        }
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
            if (e != editingEdge) {
                drawArrow(g2, e.getFrom(), e.getTo());
            }
        }
        if ((edgeStart != null || editingEdge != null) && tempEdgeNode != null) {
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
            g2.setColor(n.getColor());
        }
        g2.fillOval(x, y, 2 * r, 2 * r);
        Stroke oldStroke = g2.getStroke();
        if (n == selectedNode) {
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(2f));
        } else if ((edgeStart != null || editingEdge != null) && n == edgeTarget) {
            g2.setColor(new Color(255, 94, 14));
            g2.setStroke(new BasicStroke(2f));
        } else if (n == hoveredNode) {
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(2f));
        } else {
            g2.setColor(Color.BLACK);
        }
        g2.drawOval(x, y, 2 * r, 2 * r);
        g2.setStroke(oldStroke);
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
