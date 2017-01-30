package util.algorithms;

import util.Point;

public class Node {
    public double f;
    public double g;
    public Point position;
    
    public Node parent;

    public Node(double f, Point position, Node parent)
    {
        this.f = f;
        this.position = position;
        this.parent = parent;
    }

    public boolean equals(Node n)
    {
        return position == n.position;
    }
    
    public boolean equals(Point p){
    	return position.getX() == p.getX() && position.getY() == p.getY();
    }
}
