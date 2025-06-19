package me.wphillips.fsmedit;

import java.io.Serializable;

public class Edge implements Serializable {
    private static final long serialVersionUID = 1L;
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
