package me.wphillips.fsmedit;

import java.io.Serializable;
import java.io.IOException;

public class Edge implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Type of spline used for this edge. */
    public enum SplineType { STRAIGHT, BEZIER }

    private Node from;
    private Node to;
    private SplineType splineType;
    /** Controls the curvature when using a bezier spline. */
    private float curvature;

    public Edge(Node from, Node to) {
        this(from, to, SplineType.STRAIGHT);
    }

    public Edge(Node from, Node to, SplineType type) {
        this.from = from;
        this.to = to;
        this.splineType = type;
        this.curvature = 0.3f;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    /** Get the spline type used to render this edge. */
    public SplineType getSplineType() {
        return splineType == null ? SplineType.STRAIGHT : splineType;
    }

    /** Set the spline type used to render this edge. */
    public void setSplineType(SplineType type) {
        this.splineType = type;
    }

    /** Get the curvature used for bezier splines. */
    public float getCurvature() {
        return curvature;
    }

    /** Set the curvature used for bezier splines. */
    public void setCurvature(float curvature) {
        this.curvature = curvature;
    }

    /**
     * Update the destination node for this edge.
     */
    public void setTo(Node to) {
        this.to = to;
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (splineType == null) {
            splineType = SplineType.STRAIGHT;
        }
        if (curvature == 0f) {
            curvature = 0.3f;
        }
    }
}
