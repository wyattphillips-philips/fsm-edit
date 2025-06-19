package me.wphillips.fsmedit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable representation of a graph consisting of nodes and edges.
 */
public class GraphModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();
    private Node startNode;

    public GraphModel() {}

    public GraphModel(List<Node> nodes, List<Edge> edges, Node startNode) {
        this.nodes = new ArrayList<>(nodes);
        this.edges = new ArrayList<>(edges);
        this.startNode = startNode;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Node getStartNode() {
        return startNode;
    }
}
