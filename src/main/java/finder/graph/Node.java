package finder.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Node represents a node in a bidirectional graph. A node A can share several
 * edges with another node B.
 * 
 * @author Johan Holmberg, Malmö University
 *
 * @param <T> A data type
 */
public class Node<T> {
	private T value;
	private final List<Edge<T>> edges = new ArrayList<Edge<T>>();
	private boolean visited;
	
	/**
	 * Creates an instance of this class.
	 * 
	 * @param value The value associated with this node.
	 */
	public Node(T value) {
		this.value = value;
	}
	
	/**
	 * Gets the value associated with this node.
	 * 
	 * @return The value associated with this node.
	 */
	public T getValue() {
		return value;
	}
	
	/**
	 * Connects this node to another node if no prior edge between the nodes
	 * exist. Nodes cannot connect to themselves.
	 * 
	 * @param node The node to connect to.
	 * @return True if a new edge was created, otherwise false.
	 */
	public boolean connectTo(Node<T> node) {
		return connectTo(node, 1);
	}
	
	/**
	 * Connects this node to another node if no prior edge between the nodes
	 * exist. Nodes cannot connect to themselves.
	 * 
	 * @param node The node to connect to.
	 * @param width The width of the edge.
	 * @return True if a new edge was created, otherwise false.
	 */
	public boolean connectTo(Node<T> node, int width) {
		if (hasEdgeTo(node)) {
			return false;
		}
		
		return forciblyConnectTo(node, width);
	}
	
	/**
	 * Connects this node to another node. Nodes cannot connect to themselves.
	 * 
	 * @param node The node to connect to.
	 * @return Returns true if the connection was successful, otherwise false.
	 */
	public boolean forciblyConnectTo(Node<T> node) {
		return forciblyConnectTo(node, 1);
	}
	
	/**
	 * Connects this node to another node. Nodes cannot connect to themselves.
	 * 
	 * @param node The node to connect to.
	 * @param width The width of the edge.
	 * @return Returns true if the connection was successful, otherwise false.
	 */
	public boolean forciblyConnectTo(Node<T> node, int width) {
		if (this == node) {
			return false;
		}
		
		Edge<T> edge = new Edge<T>(this, node, width);
		addEdge(edge);
		node.addEdge(edge);
		
		return true;
	}
	
	/**
	 * Adds an edge to the edge list.
	 * 
	 * @param edge An edge.
	 */
	public void addEdge(Edge<T> edge) {
		edges.add(edge);
	}
	
	/**
	 * Disconnects this node from another node. All edges between the two nodes
	 * will be removed.
	 * 
	 * @param node The node to disconnect from.
	 */
	public void disconnectFrom(Node<T> node) {
		if (!hasEdgeTo(node)) {
			return;
		}
		
		edges.stream()
			.filter(edge -> edge.connects(this, node))
			.forEach(n -> edges.remove(n));
		node.disconnectFrom(this);
	}
	
	/**
	 * Removes an edge from this node.
	 * 
	 * @param edge The edge to remove.
	 */
	public void removeEdge(Edge<T> edge) {
		Node<T> node = edge.getNodeA();
		if (this == node) {
			node = edge.getNodeB();
		}
		if (node != null) {
			node.removeEdge(edge);
		}
		
		edges.remove(edge);
	}
	
	/**
	 * Gets all edges associated with this node.
	 * 
	 * @return A list of edges.
	 */
	public List<Edge<T>> getEdges() {
		return edges;
	}
	
	/**
	 * Returns the number of edges connected to this node.
	 * 
	 * @return The number of edges.
	 */
	public int countEdges() {
		return edges.size();
	}
	
	/**
	 * Tries to find an edge between this node and another node.
	 * 
	 * @param node The node to find an edge to.
	 * @return May or may not contain an edge.
	 */
	public Optional<Edge<T>> findEdgeTo(Node<T> node) {
		return edges.stream()
				.filter(edge -> edge.connects(this, node))
				.findFirst();
	}
	
	/**
	 * Checks whether this node shares an edge with another node.
	 * 
	 * @param node A node.
	 * @return True if the two nodes share an edge, otherwise false.
	 */
	public boolean hasEdgeTo(Node<T> node) {
		return findEdgeTo(node).isPresent();
	}
	
	/**
	 * Tries to visit this node. If the node has already been visited, it does
	 * nothing.
	 * 
	 * @return If the node was successfully visited.
	 */
	public boolean tryVisit() {
		if (visited) {
			visited = true;
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Sees if this node has been visited.
	 * 
	 * @return True if the node has been visited, otherwise false.
	 */
	public boolean isVisited() {
		return visited;
	}
	
	/**
	 *  Marks this node as unvisited.
	 */
	public void unvisit() {
		visited = false;
	}
}
