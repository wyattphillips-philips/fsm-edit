package me.wphillips.fsmedit;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.Toolkit;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Stroke;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GraphPanel extends JPanel {
    /** Maximum pixel width for edge text before wrapping occurs. */
    private static final int EDGE_TEXT_WRAP_WIDTH = 120;
    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private Node startNode;
    private Node draggedNode;
    private Node hoveredNode;
    private Node selectedNode;
    private final List<Node> selectedNodes = new ArrayList<>();
    private Point selectionStart;
    private Rectangle selectionRect;
    private PropertiesPanel propertiesPanel;
    private int lastMouseX;
    private int lastMouseY;
    private Node edgeStart;
    private Node tempEdgeNode;
    private Node edgeTarget;
    private Edge editingEdge;
    private Edge selectedEdge;
    private final GraphPopupMenu popupMenu;
    private final int menuShortcutMask;
    private final java.util.List<Node> clipboardNodes = new java.util.ArrayList<>();
    private final java.util.List<Edge> clipboardEdges = new java.util.ArrayList<>();
    private int clipboardCenterX;
    private int clipboardCenterY;

    /** Current zoom level. */
    private double scale = 1.0;
    /** Current pan offset in pixels. */
    private double translateX = 0;
    /** Current pan offset in pixels. */
    private double translateY = 0;
    /** Starting point for panning with the middle mouse button. */
    private Point panStart;
    /** Track whether the space key is held for trackpad panning. */
    private boolean spaceDown;
    /** Whether to draw the background grid. */
    private boolean showGrid;
    /** Grid spacing per square in pixels. */
    private final int gridSpacing = 10;
    /** Whether to snap nodes to the grid while dragging. */
    private boolean snapToGrid;

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

    private boolean isMenuShortcutDown(InputEvent e) {
        return (e.getModifiersEx() & menuShortcutMask) != 0;
    }

    public GraphPanel() {
        popupMenu = new GraphPopupMenu(this);
        menuShortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        showGrid = false;
        snapToGrid = false;

        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, menuShortcutMask), "copy");
        am.put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                copyContext(hoveredNode);
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, menuShortcutMask), "paste");
        am.put("paste", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Point p = getMousePosition();
                if (p == null) {
                    p = new Point(getWidth() / 2, getHeight() / 2);
                }
                pasteClipboard(screenToWorldX(p.x), screenToWorldY(p.y));
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        am.put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                java.util.List<Node> sel = getSelectedNodes();
                if (!sel.isEmpty()) {
                    removeNodes(sel);
                }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), "spaceDown");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true), "spaceUp");
        am.put("spaceDown", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                spaceDown = true;
            }
        });
        am.put("spaceUp", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                spaceDown = false;
                if (panStart == null) {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        MouseAdapter handler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isMiddleMouseButton(e) || (spaceDown && SwingUtilities.isLeftMouseButton(e))) {
                    panStart = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    return;
                }
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int x = screenToWorldX(e.getX());
                    int y = screenToWorldY(e.getY());
                    if (isMenuShortcutDown(e)) {
                        Edge edgeHit = getEdgeAt(x, y);
                        if (edgeHit != null) {
                            editingEdge = edgeHit;
                            selectedEdge = edgeHit;
                            if (propertiesPanel != null) {
                                propertiesPanel.setEdge(selectedEdge);
                            }
                            edgeStart = editingEdge.getFrom();
                            tempEdgeNode = new Node(x, y, 0, "");
                            edgeTarget = null;
                            repaint();
                            return;
                        }
                    }
                    Node hit = getNodeAt(x, y);
                    if (hit != null) {
                        if (isMenuShortcutDown(e)) {
                            edgeStart = hit;
                            tempEdgeNode = new Node(x, y, 0, "");
                            edgeTarget = null;
                            repaint();
                        } else {
                            if (!selectedNodes.contains(hit) || selectedNodes.size() <= 1) {
                                selectedNodes.clear();
                                selectedNodes.add(hit);
                            }
                            selectedNode = selectedNodes.size() == 1 ? hit : null;
                            if (propertiesPanel != null) {
                                propertiesPanel.setEdge(null);
                                propertiesPanel.setNodes(selectedNodes);
                            }
                            if (!hit.isLocked()) {
                                draggedNode = hit;
                                lastMouseX = x;
                                lastMouseY = y;
                            } else {
                                draggedNode = null;
                            }
                            selectedEdge = null;
                            repaint();
                        }
                    } else {
                        Edge edgeHit = getEdgeAt(x, y);
                        if (edgeHit != null) {
                            selectedNodes.clear();
                            selectedNode = null;
                            draggedNode = null;
                            selectedEdge = edgeHit;
                            if (propertiesPanel != null) {
                                propertiesPanel.setNodes(java.util.Collections.emptyList());
                                propertiesPanel.setEdge(selectedEdge);
                            }
                            repaint();
                        } else {
                            selectedNodes.clear();
                            selectedNode = null;
                            draggedNode = null;
                            selectedEdge = null;
                            if (!isMenuShortcutDown(e)) {
                                selectionStart = new Point(x, y);
                                selectionRect = new Rectangle(x, y, 0, 0);
                            }
                            if (propertiesPanel != null) {
                                propertiesPanel.setEdge(null);
                                propertiesPanel.setNodes(selectedNodes);
                            }
                            repaint();
                        }
                    }
                } else if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int x = screenToWorldX(e.getX());
                    int y = screenToWorldY(e.getY());
                    Node hit = getNodeAt(x, y);
                    popupMenu.showMenu(GraphPanel.this, e.getX(), e.getY(), hit);
                    return;
                }
                int hx = screenToWorldX(e.getX());
                int hy = screenToWorldY(e.getY());
                setHoveredNode(getNodeAt(hx, hy));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (panStart != null && (SwingUtilities.isMiddleMouseButton(e) || SwingUtilities.isLeftMouseButton(e))) {
                    panStart = null;
                    if (!spaceDown) {
                        setCursor(Cursor.getDefaultCursor());
                    }
                    return;
                }
                int x = screenToWorldX(e.getX());
                int y = screenToWorldY(e.getY());
                if (editingEdge != null) {
                    Node hit = getNodeAt(x, y);
                    if (hit != null && hit != edgeStart) {
                        editingEdge.setTo(hit);
                        setSplineByExistingEdges(editingEdge);
                    } else {
                        edges.remove(editingEdge);
                        if (editingEdge == selectedEdge) {
                            selectedEdge = null;
                            if (propertiesPanel != null) {
                                propertiesPanel.setEdge(null);
                                propertiesPanel.setNodes(selectedNodes);
                            }
                        }
                    }
                    editingEdge = null;
                    edgeStart = null;
                    tempEdgeNode = null;
                    edgeTarget = null;
                    repaint();
                } else if (edgeStart != null) {
                    Node hit = getNodeAt(x, y);
                    if (hit != null && hit != edgeStart) {
                        addEdge(new Edge(edgeStart, hit));
                    }
                    edgeStart = null;
                    tempEdgeNode = null;
                    edgeTarget = null;
                    repaint();
                } else if (selectionRect != null) {
                    selectedNodes.clear();
                    Rectangle r = selectionRect;
                    if (r.width < 0) {
                        r = new Rectangle(r.x + r.width, r.y, -r.width, r.height);
                    }
                    if (r.height < 0) {
                        r = new Rectangle(r.x, r.y + r.height, r.width, -r.height);
                    }
                    for (Node n : nodes) {
                        if (r.contains(n.getX(), n.getY())) {
                            selectedNodes.add(n);
                        }
                    }
                    selectedNode = selectedNodes.size() == 1 ? selectedNodes.get(0) : null;
                    if (propertiesPanel != null) {
                        propertiesPanel.setNodes(selectedNodes);
                    }
                    selectionStart = null;
                    selectionRect = null;
                    repaint();
                } else {
                    if (e.isPopupTrigger()) {
                        Node hit = getNodeAt(x, y);
                        popupMenu.showMenu(GraphPanel.this, e.getX(), e.getY(), hit);
                    }
                    draggedNode = null;
                }
                setHoveredNode(getNodeAt(x, y));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (panStart != null) {
                    int dx = e.getX() - panStart.x;
                    int dy = e.getY() - panStart.y;
                    translateX += dx;
                    translateY += dy;
                    panStart = e.getPoint();
                    repaint();
                    return;
                }
                int x = screenToWorldX(e.getX());
                int y = screenToWorldY(e.getY());
                if (editingEdge != null) {
                    tempEdgeNode.setPosition(x, y);
                    Node hit = getNodeAt(x, y);
                    if (hit != null && hit != edgeStart) {
                        edgeTarget = hit;
                    } else {
                        edgeTarget = null;
                    }
                    repaint();
                } else if (edgeStart != null) {
                    tempEdgeNode.setPosition(x, y);
                    Node hit = getNodeAt(x, y);
                    if (hit != null && hit != edgeStart) {
                        edgeTarget = hit;
                    } else {
                        edgeTarget = null;
                    }
                    repaint();
                } else if (draggedNode != null) {
                    if (!draggedNode.isLocked()) {
                        int dx = x - lastMouseX;
                        int dy = y - lastMouseY;
                        if (selectedNodes.size() > 1) {
                            for (Node n : selectedNodes) {
                                if (!n.isLocked()) {
                                    int nx = n.getX() + dx;
                                    int ny = n.getY() + dy;
                                    if (snapToGrid) {
                                        nx = snapCoord(nx);
                                        ny = snapCoord(ny);
                                    }
                                    n.setPosition(nx, ny);
                                }
                            }
                        } else {
                            int nx = draggedNode.getX() + dx;
                            int ny = draggedNode.getY() + dy;
                            if (snapToGrid) {
                                nx = snapCoord(nx);
                                ny = snapCoord(ny);
                            }
                            draggedNode.setPosition(nx, ny);
                        }
                        lastMouseX = x;
                        lastMouseY = y;
                        if (propertiesPanel != null && selectedNodes.size() == 1) {
                            propertiesPanel.updatePositionFields();
                        }
                        repaint();
                    } else {
                        draggedNode = null;
                    }
                } else if (selectionRect != null) {
                    selectionRect.width = x - selectionStart.x;
                    selectionRect.height = y - selectionStart.y;
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int x = screenToWorldX(e.getX());
                int y = screenToWorldY(e.getY());
                setHoveredNode(getNodeAt(x, y));
            }
        };

        addMouseListener(handler);
        addMouseMotionListener(handler);
        addMouseWheelListener(e -> {
            double old = scale;
            if (e.getPreciseWheelRotation() < 0) {
                scale *= 1.1;
            } else {
                scale /= 1.1;
            }
            scale = Math.max(0.1, Math.min(5.0, scale));
            double factor = scale / old;
            translateX = e.getX() - factor * (e.getX() - translateX);
            translateY = e.getY() - factor * (e.getY() - translateY);
            repaint();
        });
    }

    public void setPropertiesPanel(PropertiesPanel panel) {
        this.propertiesPanel = panel;
        if (panel != null) {
            panel.setNodes(selectedNodes);
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
        setSplineByExistingEdges(edge);
        edges.add(edge);
    }

    private void setSplineByExistingEdges(Edge edge) {
        boolean reverse = false;
        for (Edge e : edges) {
            if (e != edge && e.getFrom() == edge.getTo() && e.getTo() == edge.getFrom()) {
                reverse = true;
                break;
            }
        }
        edge.setSplineType(reverse ? Edge.SplineType.BEZIER : Edge.SplineType.STRAIGHT);
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
        edges.removeIf(e -> {
            if (e.getFrom() == node || e.getTo() == node) {
                if (e == selectedEdge) {
                    selectedEdge = null;
                    if (propertiesPanel != null) {
                        propertiesPanel.setEdge(null);
                    }
                }
                return true;
            }
            return false;
        });
        if (startNode == node) {
            startNode = null;
        }
        if (selectedNode == node) {
            selectedNode = null;
            if (propertiesPanel != null) {
                propertiesPanel.setNodes(selectedNodes);
            }
        }
        selectedNodes.remove(node);
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
     * Return the topmost edge that is near the given coordinates. The hitbox
     * includes the entire curve rather than just the arrow head.
     */
    private Edge getEdgeAt(int x, int y) {
        final double threshold = 10.0;
        for (int i = edges.size() - 1; i >= 0; i--) {
            Edge e = edges.get(i);
            if (e.getSplineType() == Edge.SplineType.BEZIER) {
                Point cp = bezierControlPoint(e.getFrom(), e.getTo(), e.getCurvature());
                Point p1 = boundaryPoint(e.getFrom(), cp.x, cp.y);
                Point p2 = boundaryPoint(e.getTo(), cp.x, cp.y);
                if (bezierDistance(p1, cp, p2, x, y) <= threshold) {
                    return e;
                }
            } else {
                Point p1 = boundaryPoint(e.getFrom(), e.getTo());
                Point p2 = boundaryPoint(e.getTo(), e.getFrom());
                if (Line2D.ptSegDist(p1.x, p1.y, p2.x, p2.y, x, y) <= threshold) {
                    return e;
                }
            }
        }
        return null;
    }

    private Point bezierControlPoint(Node from, Node to, float curvature) {
        int x1 = from.getX();
        int y1 = from.getY();
        int x2 = to.getX();
        int y2 = to.getY();

        float midX = (x1 + x2) / 2f;
        float midY = (y1 + y2) / 2f;
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) dist = 1f;
        float nx = -dy / dist;
        float ny = dx / dist;
        float offset = curvature * dist;
        int cx = Math.round(midX + nx * offset);
        int cy = Math.round(midY + ny * offset);
        return new Point(cx, cy);
    }

    /**
     * Approximate the distance from a point to a quadratic bezier curve.
     */
    private double bezierDistance(Point p1, Point cp, Point p2, int x, int y) {
        double min = Double.MAX_VALUE;
        double prevX = p1.x;
        double prevY = p1.y;
        for (int i = 1; i <= 20; i++) {
            double t = i / 20.0;
            double it = 1 - t;
            double bx = it * it * p1.x + 2 * it * t * cp.x + t * t * p2.x;
            double by = it * it * p1.y + 2 * it * t * cp.y + t * t * p2.y;
            double d = Line2D.ptSegDist(prevX, prevY, bx, by, x, y);
            if (d < min) {
                min = d;
            }
            prevX = bx;
            prevY = by;
        }
        return min;
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
        selectedNodes.clear();
        selectedEdge = null;
        hoveredNode = null;
        draggedNode = null;
        editingEdge = null;
        edgeStart = null;
        tempEdgeNode = null;
        edgeTarget = null;
        scale = 1.0;
        translateX = 0;
        translateY = 0;
        if (propertiesPanel != null) {
            propertiesPanel.setNodes(selectedNodes);
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
        selectedNodes.clear();
        selectedEdge = null;
        hoveredNode = null;
        draggedNode = null;
        editingEdge = null;
        edgeStart = null;
        tempEdgeNode = null;
        edgeTarget = null;
        scale = 1.0;
        translateX = 0;
        translateY = 0;
        if (propertiesPanel != null) {
            propertiesPanel.setNodes(selectedNodes);
        }
        repaint();
    }

    /**
     * Reset the zoom and pan settings to their defaults.
     */
    public void resetView() {
        scale = 1.0;
        translateX = 0;
        translateY = 0;
        repaint();
    }

    /**
     * Check whether the background grid is currently shown.
     */
    public boolean isShowGrid() {
        return showGrid;
    }

    /**
     * Set whether the background grid should be displayed.
     */
    public void setShowGrid(boolean show) {
        this.showGrid = show;
        repaint();
    }

    /** Check whether node dragging snaps to the grid. */
    public boolean isSnapToGrid() {
        return snapToGrid;
    }

    /** Enable or disable snapping nodes to the grid while dragging. */
    public void setSnapToGrid(boolean snap) {
        this.snapToGrid = snap;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.translate(translateX, translateY);
        g2.scale(scale, scale);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (showGrid) {
            int spacing = gridSpacing;
            int majorSpacing = spacing * 5;
            int left = screenToWorldX(0);
            int right = screenToWorldX(getWidth());
            int top = screenToWorldY(0);
            int bottom = screenToWorldY(getHeight());

            int startX = (int) (Math.floor(left / (double) spacing) * spacing);
            int startY = (int) (Math.floor(top / (double) spacing) * spacing);

            Stroke oldStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(0.5f));
            Color minor = new Color(200, 200, 200, 80);
            Color major = new Color(160, 160, 160, 120);

            for (int x = startX; x <= right; x += spacing) {
                if (x == 0) continue;
                g2.setColor(x % majorSpacing == 0 ? major : minor);
                g2.drawLine(x, top, x, bottom);
            }
            for (int y = startY; y <= bottom; y += spacing) {
                if (y == 0) continue;
                g2.setColor(y % majorSpacing == 0 ? major : minor);
                g2.drawLine(left, y, right, y);
            }

            g2.setStroke(new BasicStroke(1f));
            g2.setColor(new Color(255, 0, 0, 150));
            g2.drawLine(left, 0, right, 0);
            g2.setColor(new Color(0, 0, 255, 150));
            g2.drawLine(0, top, 0, bottom);
            g2.setStroke(oldStroke);
        }

        // Draw edges first
        g2.setColor(Color.BLACK);
        for (Edge e : edges) {
            if (e != editingEdge) {
                Stroke old = g2.getStroke();
                if (e == selectedEdge) {
                    g2.setColor(Color.RED);
                    g2.setStroke(new BasicStroke(2f));
                }
                drawArrow(g2, e);
                drawEdgeText(g2, e);
                if (e == selectedEdge) {
                    g2.setStroke(old);
                    g2.setColor(Color.BLACK);
                }
            }
        }
        if ((edgeStart != null || editingEdge != null) && tempEdgeNode != null) {
            drawArrow(g2, edgeStart, new Point(tempEdgeNode.getX(), tempEdgeNode.getY()));
        }

        // Draw nodes on top
        for (Node n : nodes) {
            drawNode(g2, n);
        }

        if (selectionRect != null) {
            Rectangle r = selectionRect;
            if (r.width < 0) {
                r = new Rectangle(r.x + r.width, r.y, -r.width, r.height);
            }
            if (r.height < 0) {
                r = new Rectangle(r.x, r.y + r.height, r.width, -r.height);
            }
            Stroke old = g2.getStroke();
            g2.setColor(Color.GRAY);
            float[] dash = {4f};
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
            g2.drawRect(r.x, r.y, r.width, r.height);
            g2.setStroke(old);
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
        if (selectedNodes.contains(n)) {
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

    private void drawArrow(Graphics2D g2, Edge edge) {
        if (edge.getSplineType() == Edge.SplineType.BEZIER) {
            drawBezierArrow(g2, edge.getFrom(), edge.getTo(), edge.getCurvature());
        } else {
            drawArrow(g2, edge.getFrom(), edge.getTo());
        }
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

    private void drawBezierArrow(Graphics2D g2, Node from, Node to, float curvature) {
        // compute control point using node centres so connection points can
        // depend on the curvature
        int x1 = from.getX();
        int y1 = from.getY();
        int x2 = to.getX();
        int y2 = to.getY();

        float midX = (x1 + x2) / 2f;
        float midY = (y1 + y2) / 2f;
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) dist = 1f;
        float nx = -dy / dist;
        float ny = dx / dist;
        float offset = curvature * dist;
        float cx = midX + nx * offset;
        float cy = midY + ny * offset;

        // determine boundary points relative to the control point so that the
        // curve attaches to the node slightly above or below the middle
        Point p1 = boundaryPoint(from, (int) cx, (int) cy);
        Point p2 = boundaryPoint(to, (int) cx, (int) cy);

        QuadCurve2D.Float curve = new QuadCurve2D.Float(p1.x, p1.y, cx, cy, p2.x, p2.y);
        g2.draw(curve);

        // orientation for arrow head along tangent at the end
        double tx = p2.x - cx;
        double ty = p2.y - cy;
        double len = Math.sqrt(tx * tx + ty * ty);
        if (len == 0) len = 1;
        Point base = new Point((int) Math.round(p2.x - tx / len * 1), (int) Math.round(p2.y - ty / len * 1));
        drawArrowHead(g2, base, p2);
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

    /** Draw any text associated with the given edge near its midpoint. */
    private void drawEdgeText(Graphics2D g2, Edge edge) {
        String text = edge.getText();
        if (text == null || text.isEmpty()) {
            return;
        }

        Point pos;
        if (edge.getSplineType() == Edge.SplineType.BEZIER) {
            Point cp = bezierControlPoint(edge.getFrom(), edge.getTo(), edge.getCurvature());
            Point p1 = boundaryPoint(edge.getFrom(), cp.x, cp.y);
            Point p2 = boundaryPoint(edge.getTo(), cp.x, cp.y);
            double mx = 0.25 * p1.x + 0.5 * cp.x + 0.25 * p2.x;
            double my = 0.25 * p1.y + 0.5 * cp.y + 0.25 * p2.y;
            pos = new Point((int) Math.round(mx), (int) Math.round(my));
        } else {
            Point p1 = boundaryPoint(edge.getFrom(), edge.getTo());
            Point p2 = boundaryPoint(edge.getTo(), edge.getFrom());
            pos = new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
        }

        FontMetrics fm = g2.getFontMetrics();
        java.util.List<String> lines = wrapText(fm, text, EDGE_TEXT_WRAP_WIDTH);
        int ascent = fm.getAscent();
        int descent = fm.getDescent();
        int lineHeight = ascent + descent;
        int textHeight = lineHeight * lines.size();
        int textWidth = 0;
        for (String line : lines) {
            textWidth = Math.max(textWidth, fm.stringWidth(line));
        }

        int centerY = pos.y - 4;
        int top = centerY - textHeight / 2;
        int x = pos.x - textWidth / 2;

        Color prev = g2.getColor();
        g2.setColor(Color.WHITE);
        g2.fillRect(x - 2, top - 2, textWidth + 4, textHeight + 4);
        g2.setColor(Color.BLACK);
        g2.drawRect(x - 2, top - 2, textWidth + 4, textHeight + 4);

        int baseline = top + ascent;
        for (String line : lines) {
            int lineWidth = fm.stringWidth(line);
            g2.drawString(line, pos.x - lineWidth / 2, baseline);
            baseline += lineHeight;
        }

        g2.setColor(prev);
    }

    /**
     * Break the given text into multiple lines so each line fits within the specified width.
     */
    private static java.util.List<String> wrapText(FontMetrics fm, String text, int maxWidth) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (line.length() == 0) {
                if (fm.stringWidth(word) <= maxWidth) {
                    line.append(word);
                } else {
                    splitWord(lines, fm, word, maxWidth);
                }
                continue;
            }

            String candidate = line + " " + word;
            if (fm.stringWidth(candidate) <= maxWidth) {
                line.append(' ').append(word);
            } else {
                lines.add(line.toString());
                line.setLength(0);
                if (fm.stringWidth(word) <= maxWidth) {
                    line.append(word);
                } else {
                    splitWord(lines, fm, word, maxWidth);
                }
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    /** Break a long word into multiple lines. */
    private static void splitWord(java.util.List<String> lines, FontMetrics fm, String word, int maxWidth) {
        int start = 0;
        while (start < word.length()) {
            int end = start + 1;
            while (end <= word.length() && fm.stringWidth(word.substring(start, end)) <= maxWidth) {
                end++;
            }
            end--;
            lines.add(word.substring(start, end));
            start = end;
        }
    }

    /**
     * Get a snapshot of the currently selected nodes.
     */
    public java.util.List<Node> getSelectedNodes() {
        return new java.util.ArrayList<>(selectedNodes);
    }

    /**
     * Remove multiple nodes at once. A copy of the input list is used
     * so callers can pass {@link #getSelectedNodes()} directly.
     */
    public void removeNodes(java.util.List<Node> nodesToRemove) {
        for (Node n : new java.util.ArrayList<>(nodesToRemove)) {
            removeNode(n);
        }
        clearSelection();
    }

    /**
     * Clear the current selection and update the properties panel.
     */
    public void clearSelection() {
        selectedNode = null;
        selectedNodes.clear();
        selectedEdge = null;
        if (propertiesPanel != null) {
            propertiesPanel.setNodes(selectedNodes);
        }
        repaint();
    }

    /** Copy the given nodes and any connecting edges to the internal clipboard. */
    private void copyNodes(java.util.List<Node> nodesToCopy) {
        clipboardNodes.clear();
        clipboardEdges.clear();
        if (nodesToCopy.isEmpty()) {
            return;
        }
        int sumX = 0;
        int sumY = 0;
        java.util.Map<Node, Node> map = new java.util.HashMap<>();
        for (Node n : nodesToCopy) {
            Node c = cloneNode(n);
            clipboardNodes.add(c);
            map.put(n, c);
            sumX += n.getX();
            sumY += n.getY();
        }
        clipboardCenterX = Math.round((float) sumX / nodesToCopy.size());
        clipboardCenterY = Math.round((float) sumY / nodesToCopy.size());
        for (Edge e : edges) {
            if (map.containsKey(e.getFrom()) && map.containsKey(e.getTo())) {
                Edge ec = new Edge(map.get(e.getFrom()), map.get(e.getTo()), e.getSplineType());
                ec.setCurvature(e.getCurvature());
                ec.setText(e.getText());
                clipboardEdges.add(ec);
            }
        }
    }

    /** Clone a node including its visual properties. */
    private static Node cloneNode(Node n) {
        Node c = new Node(n.getX(), n.getY(), n.getRadius(), n.getLabel(), n.getColor());
        c.setMetadata(n.getMetadata());
        c.setLocked(n.isLocked());
        return c;
    }

    /** Invoked by the popup to copy either the selection or a single node. */
    public void copyContext(Node clicked) {
        if (!selectedNodes.isEmpty() && (clicked == null || selectedNodes.contains(clicked))) {
            copyNodes(selectedNodes);
        } else if (clicked != null) {
            java.util.List<Node> single = new java.util.ArrayList<>();
            single.add(clicked);
            copyNodes(single);
        }
    }

    /** Paste the clipboard contents centered at the given location. */
    public void pasteClipboard(int x, int y) {
        if (clipboardNodes.isEmpty()) {
            return;
        }
        int dx = x - clipboardCenterX;
        int dy = y - clipboardCenterY;
        java.util.Map<Node, Node> map = new java.util.HashMap<>();
        for (Node n : clipboardNodes) {
            Node c = cloneNode(n);
            c.moveBy(dx, dy);
            nodes.add(c);
            map.put(n, c);
        }
        for (Edge e : clipboardEdges) {
            Node from = map.get(e.getFrom());
            Node to = map.get(e.getTo());
            if (from != null && to != null) {
                Edge ec = new Edge(from, to, e.getSplineType());
                ec.setCurvature(e.getCurvature());
                ec.setText(e.getText());
                edges.add(ec);
            }
        }
        selectedNodes.clear();
        selectedNodes.addAll(map.values());
        selectedNode = selectedNodes.size() == 1 ? selectedNodes.get(0) : null;
        if (propertiesPanel != null) {
            propertiesPanel.setNodes(selectedNodes);
        }
        repaint();
    }

    /** Check if the clipboard currently contains nodes. */
    public boolean hasClipboard() {
        return !clipboardNodes.isEmpty();
    }

    /** Convert an x-coordinate from screen space to world space. */
    public int screenToWorldX(int x) {
        return (int) Math.round((x - translateX) / scale);
    }

    /** Convert a y-coordinate from screen space to world space. */
    public int screenToWorldY(int y) {
        return (int) Math.round((y - translateY) / scale);
    }

    /** Convert a point from screen space to world space. */
    public Point screenToWorld(Point p) {
        return new Point(screenToWorldX(p.x), screenToWorldY(p.y));
    }

    /** Round a coordinate to the nearest grid intersection. */
    private int snapCoord(int v) {
        return Math.round(v / (float) gridSpacing) * gridSpacing;
    }
}
