package finder.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Graph represents a bidirectional graph.
 * 
 * @author Johan Holmberg, Malmö University
 *
 * @param <T> A data type.
 */
public class Graph<T> {

	private final Map<T, Node<T>> nodes = new HashMap<T, Node<T>>();
	
	/**
	 * Adds a new node to the graph. Each value can only be added once.
	 * 
	 * @param value A value that will be represented by the node.
	 * @return True if the node was added, otherwise false.
	 */
	public boolean addNode(T value) {
		if (containsNode(value)) {
			return false;
		}
		
		nodes.put(value, new Node<T>(value));
		return true;
	}
	
	/**
	 * Removes a node from the graph.
	 * 
	 * @param value The value to remove.
	 * @return True if the node was removed, otherwise false.
	 */
	public boolean removeNode(T value) {
		Node<T> node = getNode(value);
		List<Edge<T>> edges = null;
		
		if (node == null || node.countEdges() == 0) {
			return false;
		}
		
		edges = node.getEdges();
		
		for (Edge<T> edge: edges) {
			node.removeEdge(edge);
		}
		
		return false;
	}
	
	/**
	 * Checks whether the graph contains a node.
	 * 
	 * @param value The value to search for.
	 * @return True if the node exists, otherwise false.
	 */
	public boolean containsNode(T value) {
		return nodes.containsKey(value);
	}
	
	/**
	 * Retrieves a node from the graph.
	 * 
	 * @param value The value to search for.
	 * @return The node, or null if not found.
	 */
	public Node<T> getNode(T value) {
		return nodes.get(value);
	}
	
	/**
	 * Connects two nodes in the graph with each other. The nodes will only be
	 * connected if they are not already connected.
	 * 
	 * @param a A node.
	 * @param b Another node.
	 * @return Returns true if the nodes were connected, otherwise false.
	 */
	public boolean connect(T a, T b) {
		return connect(a, b, 1);
	}
	
	/**
	 * Connects two nodes in the graph with each other. The nodes will only be
	 * connected if they are not already connected.
	 * 
	 * @param a A node.
	 * @param b Another node.
	 * @param width The edge's width.
	 * @return Returns true if the nodes were connected, otherwise false.
	 */
	public boolean connect(T a, T b, int width) {
		Node<T> nodeA = new Node<T>(a);
		Node<T> nodeB = new Node<T>(b);
		
		return nodeA.connectTo(nodeB, width);
	}
	
	/**
	 * Forcefully connects two nodes in the graph with each other.
	 * 
	 * @param a A node.
	 * @param b Another node.
	 * @return Returns true if the nodes were connected, otherwise false.
	 */
	public boolean forceConnect(T a, T b, int width) {
		Node<T> nodeA = new Node<T>(a);
		Node<T> nodeB = new Node<T>(b);
		
		return nodeA.forciblyConnectTo(nodeB, width);
	}
	
	/**
	 * Forcefully connects two nodes in the graph with each other.
	 * 
	 * @param a A node.
	 * @param b Another node.
	 * @param width The edge's width.
	 * @return Returns true if the nodes were connected, otherwise false.
	 */
	public boolean disconnect(T a, T b) {
		Node<T> nodeA = getNode(a);
		Node<T> nodeB = getNode(b);
		
		if (nodeA.hasEdgeTo(nodeB)) {
			nodeA.disconnectFrom(nodeB);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Resets the graph for searches.
	 */
	public void resetGraph() {
		nodes.keySet().forEach(key -> {
			Node<T> node = getNode(key);
			node.unvisit();
		});
	}
}
