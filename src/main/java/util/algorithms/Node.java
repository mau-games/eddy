package util.algorithms;

import javafx.geometry.Point2D;

public class Node {
    public double f;
    public double g;
    public Point2D position;
    
    public Node parent;

    public Node(double f, Point2D position, Node parent)
    {
        this.f = f;
        this.position = position;
        this.parent = parent;
    }

    public boolean equals(Node n)
    {
        return position == n.position;
    }
    
    public boolean equals(Point2D p){
    	return position.getX() == p.getX() && position.getY() == p.getY();
    }
}
