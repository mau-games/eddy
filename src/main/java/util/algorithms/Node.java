package util.algorithms;

public class Node {
    public float f;
    public Point position;
    public Node parent;

    public Node(float f, Point position, Node parent)
    {
        this.f = f;
        this.position = position;
        this.parent = parent;
    }

    public boolean Equals(Node n)
    {
        return this.position == n.position;
    }
}
