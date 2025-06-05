package me.wphillips.fsmedit;

public class Node {
    private int x;
    private int y;
    private int radius;
    private String label;

    public Node(int x, int y, int radius, String label) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.label = label;
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
}
