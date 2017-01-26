package util.algorithms;

import javafx.geometry.Point2D;

public class Node {
    public float f;
    public Point2D position;
    
    public Node parent;

    public Node(float f, Point2D position, Node parent)
    {
        this.f = f;
        this.position = position;
        this.parent = parent;
    }

    public boolean equals(Node n)
    {
        return position == n.position;
    }
}
