package finder.graph;

/**
 * Edge represents an edge in a bidirectional graph. Each edge in this graph
 * has a width, which is not to be confused with a weight. The width simply
 * denotes the number of adjacent tiles in a dungeon map that constitute this
 * edge.
 * 
 * @author Johan Holmberg, Malmö University
 *
 * @param <T> A data type.
 */
public class Edge<T> {
	private int width = 1;
	
	private Node<T> a = null;
	private Node<T> b = null;
	
	/**
	 * Creates an edge between a node A and a node B with a width of 1.
	 * 
	 * @param a Node A
	 * @param b Node B
	 */
	public Edge(Node<T> a, Node<T> b) {
		this.a = a;
		this.b = b;
	}
	
	/**
	 * Creates and edge between a node A and a node B with a given width.
	 * 
	 * @param a
	 * @param b
	 * @param width
	 */
	public Edge(Node<T> a, Node<T> b, int width) {
		this.a = a;
		this.b = b;
		this.width = width;
	}
	
	/**
	 * Widens this edge and returns the new width.
	 * 
	 * @return The width.
	 */
	public int widen() {
		return ++width;
	}
	
	/**
	 * Gets the width of this edge.
	 * 
	 * @return The width.
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Gets node A.
	 * 
	 * @return A node.
	 */
	public Node<T> getNodeA() {
		return a;
	}
	
	/**
	 * Gets node B.
	 * 
	 * @return A node.
	 */
	public Node<T> getNodeB() {
		return b;
	}
	
	/**
	 * Checks whether two nodes are connected by this edge.
	 * 
	 * @param a A node.
	 * @param b Another node.
	 * @return True if the nodes are connected, otherwise false.
	 */
	public boolean connects(Node<T> a, Node<T> b) {
		return (this.a == a || this.a == b) && (this.b == a || this.b == b);
	}
	
	/**
	 * Checks whether two objects are connected by this edge.
	 * 
	 * @param a An object.
	 * @param b Another object.
	 * @return True if the objects are connected, otherwise false.
	 */
	public boolean connects(T a, T b) {
		return (this.a.getValue() == a || this.a.getValue() == b)
				&& (this.b.getValue() == a || this.b.getValue() == b);
	}
}
