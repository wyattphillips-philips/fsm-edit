package me.wphillips.fsmedit;

import java.awt.Color;
import java.io.Serializable;

public class Node implements Serializable {
    private static final long serialVersionUID = 1L;
    private int x;
    private int y;
    private int radius;
    private String label;
    private Color color;

    public Node(int x, int y, int radius, String label) {
        this(x, y, radius, label, Color.WHITE);
    }

    public Node(int x, int y, int radius, String label, Color color) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.label = label;
        this.color = color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRadius() {
        return radius;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Update the center position of this node.
     *
     * @param x new x-coordinate
     * @param y new y-coordinate
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Move the node relative to its current position.
     *
     * @param dx change in x-coordinate
     * @param dy change in y-coordinate
     */
    public void moveBy(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    /**
     * Check if a point lies within this node.
     *
     * @param px x-coordinate of the point
     * @param py y-coordinate of the point
     * @return true if the point is inside the node
     */
    public boolean contains(int px, int py) {
        int dx = px - x;
        int dy = py - y;
        return dx * dx + dy * dy <= radius * radius;
    }
}
