package me.wphillips.fsmedit;

public class Edge {
    private Node from;
    private Node to;

    public Edge(Node from, Node to) {
        this.from = from;
        this.to = to;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    /**
     * Update the destination node for this edge.
     */
    public void setTo(Node to) {
        this.to = to;
    }
}
