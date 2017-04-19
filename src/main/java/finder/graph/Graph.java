package finder.graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import finder.patterns.InventorialPattern;
import finder.patterns.Pattern;
import finder.patterns.meso.TreasureRoom;
import finder.patterns.micro.Room;
import finder.patterns.micro.Treasure;

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
	 * Returns the first node in the graph map.
	 * 
	 * @return A node.
	 */
	 public Node<T> getStartingPoint() {
		 return nodes.entrySet().iterator().next().getValue();
	 }
	 
	 /**
	  * Counts the number of nodes in the graph.
	  * 
	  * @return The number of nodes.
	  */
	 public int countNodes() {
		 return nodes.size();
	 }
	 
	 /**
	  * Gets the list of nodes.
	  * 
	  * @return A list of nodes.
	  */
	 public Map<T, Node<T>> getNodes() {
		 return nodes;
	 }
	
	/**
	 * Adds a new node to the graph. Each value can only be added once.
	 * 
	 * @param value A value that will be represented by the node.
	 * @return The newly added node. Null if not added.
	 */
	public Node<T> addNode(T value) {
		if (containsNode(value)) {
			return null;
		}

		Node<T> node = new Node<T>(value);
		nodes.put(value, node);
		return node;
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
	 * TODO: Alex - Why would you ever want to do this?
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
	 * TODO: Alex - Why would you ever want to do this?
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
	
	public boolean isEdgeInCycle(Edge<T> edge) {
		Map<Node<T>,Boolean> tempVisited = new HashMap<Node<T>,Boolean>();
		
		Queue<Node<T>> nodeQueue = new LinkedList<Node<T>>();
		nodeQueue.add(edge.getNodeA());
		tempVisited.put(edge.getNodeA(), true);
		
		while(!nodeQueue.isEmpty()){
			Node<T> current = nodeQueue.remove();

			if(current == edge.getNodeB())
				return true;
			
			for(Edge<T> e : current.getEdges()){
				if(e != edge){
					if(e.getNodeA() == current){
						if(!tempVisited.containsKey(e.getNodeB())){
							nodeQueue.add(e.getNodeB());
							tempVisited.put(e.getNodeB(), true);
						}
					} else {
						if(!tempVisited.containsKey(e.getNodeA())){
							nodeQueue.add(e.getNodeA());
							tempVisited.put(e.getNodeA(), true);
						}
					}
				}
			}
		}

		return false;
	}
}
