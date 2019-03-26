package finder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import game.Room;
import finder.graph.Graph;
import finder.graph.Node;
import finder.patterns.CompositePattern;
import finder.patterns.Pattern;
import finder.patterns.SpacialPattern;
import finder.patterns.meso.Ambush;
import finder.patterns.meso.ChokePoint;
import finder.patterns.meso.DeadEnd;
import finder.patterns.meso.GuardRoom;
import finder.patterns.meso.GuardedTreasure;
import finder.patterns.meso.TreasureRoom;
import finder.patterns.micro.Corridor;
import finder.patterns.micro.Door;
import finder.patterns.micro.Enemy;
import finder.patterns.micro.Entrance;
import finder.patterns.micro.Nothing;
import finder.patterns.micro.Chamber;
import finder.patterns.micro.Treasure;
import finder.geometry.Bitmap;
import finder.geometry.Point;

/**
 * PatternFinder is used to find patterns within a room.
 * 
 * @author Johan Holmberg
 */
public class PatternFinder {
	
	private Room room;
	private List<Pattern> micropatterns = null;
	private List<CompositePattern> mesopatterns = null;
	private List<CompositePattern> macropatterns = null;
	private Graph<Pattern> patternGraph = null;
	private SpacialPattern[][] spacialPatternGrid = null;
	
	/**
	 * Creates a pattern finder instance.
	 * 
	 * @param room The map to search in.
	 */
	public PatternFinder(Room room) {
		this.room = room;
		spacialPatternGrid = new SpacialPattern[room.getRowCount()][room.getColCount()];
	}
	
	// TODO: Implement this
	/**
	 * Finds micro patterns within a map. It searches for all patterns
	 * available in the finder.patterns.micro package.
	 * 
	 * @return A list of all found pattern instances.
	 */
	public List<Pattern> findMicroPatterns() {
		if (micropatterns != null) {
			return micropatterns;
		}
		
		micropatterns = new ArrayList<Pattern>();
		
		/*
		 * Do this:
		 * 1. Get all patterns in finder.patterns.micro
		 * 2. Do a pattern search for each pattern for the specified map
		 * 3. Save the found patterns
		 * 4. Return all found pattern instances
		 * 
		 * MAYBE: Specify which patterns to look for in the config?
		 * 
		 * For now, let's just implicitly call each pattern.
		 */
		micropatterns.addAll(Corridor.matches(room, null)); // This also finds connectors
		micropatterns.addAll(Chamber.matches(room, null));
		micropatterns.addAll(Treasure.matches(room, null));
		micropatterns.addAll(Enemy.matches(room, null));
		micropatterns.addAll(Door.matches(room, null));
		micropatterns.addAll(Entrance.matches(room, null));
		micropatterns.addAll(Nothing.matches(room, null)); // This MUST come last
		
		return micropatterns;
	}
	
	// TODO: Implement this
	/**
	 * Finds meso patterns within a map. It searches for all patterns available
	 * in the finder.patterns.meso package.
	 * 
	 * @return A list of all found pattern instances.
	 */
	public List<CompositePattern> findMesoPatterns() {
		if(mesopatterns != null){
			return mesopatterns;
		}
		mesopatterns = new ArrayList<CompositePattern>();

		/*
		 * Do this:
		 * 1. Get all patterns in finder.patterns.meso
		 * 2. Do a pattern search for each pattern for the specified map
		 * 	  using found micro patterns
		 * 3. Save the found patterns
		 * 4. Return all found pattern instances
		 * 
		 * MAYBE: Specify which patterns to look for in the config?
		 */
		if (micropatterns == null) {
			findMicroPatterns();
		}
		
		Populator.populate(micropatterns);
		buildPatternGraph();
		
		//TODO: We need to fix this for the new way we are handling this
		mesopatterns.addAll(ChokePoint.matches(room, patternGraph));
		mesopatterns.addAll(DeadEnd.matches(room, patternGraph));
		mesopatterns.addAll(GuardRoom.matches(room, patternGraph));
		mesopatterns.addAll(TreasureRoom.matches(room, patternGraph));
		mesopatterns.addAll(Ambush.matches(room, patternGraph));
		mesopatterns.addAll(GuardedTreasure.matches(room, patternGraph)); //Do this last!
		return mesopatterns;
	}
	
	public List<CompositePattern> getMesoPatterns(){
		return findMesoPatterns();
	}
	
	
	public List<Pattern> getMicroPatterns(){
		return findMicroPatterns();
	}
	
	
	private void buildPatternGraph(){
		//Build the pattern graph
		assignSpacialPatternsToGrid();
		
		//TODO: THIS STILL NEEDS A LOT OF WORK
		
		boolean visitedTiles[][] = new boolean[room.getRowCount()][room.getColCount()];
		
		patternGraph = new Graph<Pattern>();
		Entrance entrance = (Entrance)micropatterns.stream().filter((Pattern p) -> {return p instanceof Entrance;}).findFirst().get();
		Point entrancePosition = (Point)entrance.getGeometry();		
		
//		Door door = (Door)micropatterns.stream().filter((Pattern p) -> {return p instanceof Door;}).fin
//		Point doorPos = (Point)door.getGeometry();		
		
//		Node<Pattern> start = patternGraph.addNode(spacialPatternGrid[entrancePosition.getY()][entrancePosition.getX()]);
		
		//Do a flood fill from this pattern to find all patterns
		Queue<Node<Pattern>> patternQueue = new LinkedList<Node<Pattern>>();

//		if(start.getValue() != null)
//		patternQueue.add(start);
		
		for(Pattern microPattern : micropatterns)
		{
			if(microPattern instanceof Door)
			{
				Point doorPos = (Point)microPattern.getGeometry();
				if(!patternGraph.containsNode(spacialPatternGrid[doorPos.getY()][doorPos.getX()]))
				{
					patternQueue.add(patternGraph.addNode(spacialPatternGrid[doorPos.getY()][doorPos.getX()]));
				}	
			}
		}

	
		
		while(!patternQueue.isEmpty()){
			//TODO: PROBLEM IS HERE
			Node<Pattern> currentPattern = patternQueue.remove();
			//currentPattern.tryVisit();
			
			//Do a flood fill from a point in this pattern to find adjacent patterns
			Queue<Point> tileQueue = new LinkedList<Point>();
			
			tileQueue.add(((Bitmap)currentPattern.getValue().getGeometry()).getPoint(0));
			visitedTiles[((Bitmap)currentPattern.getValue().getGeometry()).getPoint(0).getY()][((Bitmap)currentPattern.getValue().getGeometry()).getPoint(0).getX()] = true;
			
			List<Point> adjacentTiles = new ArrayList<Point>();
			
			//After this loop, adjacentTiles should contain all of the pattern tiles adjacent to this pattern
			while(!tileQueue.isEmpty()){
				
				Point currentPoint = tileQueue.remove();
				int x = currentPoint.getX();
				int y = currentPoint.getY();
				
				//Add neighbouring tiles
				if(x > 0 && !visitedTiles[y][x-1]){
					if(spacialPatternGrid[y][x-1] == currentPattern.getValue()){
						tileQueue.add(new Point(x-1,y));
						visitedTiles[y][x-1] = true;
					} else if(spacialPatternGrid[y][x-1] != null){
						Point adjacentPoint = new Point(x-1,y);
						if(adjacentTiles.stream().noneMatch((Point p) -> {return p.equals(adjacentPoint);})){
							adjacentTiles.add(adjacentPoint);
						}
					}
				}
				if(x < room.getColCount() - 1 && !visitedTiles[y][x+1]){
					if(spacialPatternGrid[y][x+1] == currentPattern.getValue()){
						tileQueue.add(new Point(x+1,y));
						visitedTiles[y][x+1] = true;
					} else if (spacialPatternGrid[y][x+1] != null){
						Point adjacentPoint = new Point(x+1,y);
						if(adjacentTiles.stream().noneMatch((Point p) -> {return p.equals(adjacentPoint);})){
							adjacentTiles.add(adjacentPoint);
						}
					}
				}
				if(y > 0 && !visitedTiles[y-1][x]){
					if(spacialPatternGrid[y-1][x] == currentPattern.getValue()){
						tileQueue.add(new Point(x,y-1));
						visitedTiles[y-1][x] = true;
					} else if (spacialPatternGrid[y-1][x] != null){
						Point adjacentPoint = new Point(x,y-1);
						if(adjacentTiles.stream().noneMatch((Point p) -> {return p.equals(adjacentPoint);})){
							adjacentTiles.add(adjacentPoint);
						}
					}
				}
				if(y < room.getRowCount() - 1 && !visitedTiles[y+1][x]){
					if(spacialPatternGrid[y+1][x] == currentPattern.getValue()){
						tileQueue.add(new Point(x,y+1));
						visitedTiles[y+1][x] = true;
					} else if (spacialPatternGrid[y+1][x] != null){
						Point adjacentPoint = new Point(x,y+1);
						if(adjacentTiles.stream().noneMatch((Point p) -> {return p.equals(adjacentPoint);})){
							adjacentTiles.add(adjacentPoint);
						}
					}
				}
				
			}
			
			//Create the new nodes
			for(Point p : adjacentTiles){
				Node<Pattern> n = patternGraph.addNode(spacialPatternGrid[p.getY()][p.getX()]);
				if(n != null){
					patternQueue.add(n);
				}
			}
			
			//For each adjacent pattern, determine the number an "width" of edges and add to the graph.
			while(!adjacentTiles.isEmpty()){
				List<Point> visited = new ArrayList<Point>();
				Queue<Point> adjacencyQueue = new LinkedList<Point>();
				adjacencyQueue.add(adjacentTiles.remove(0));
				
				while(!adjacencyQueue.isEmpty()){
					Point currentPoint = adjacencyQueue.remove();
					visited.add(currentPoint);
					adjacencyQueue.addAll(adjacentTiles.stream().filter((Point p)->{return adjacent(currentPoint,p) && samePattern(currentPoint,p);}).collect(Collectors.toList()));
					adjacentTiles.removeIf((Point p)->{return adjacent(currentPoint,p) && samePattern(currentPoint,p);});
				}
				
				currentPattern.forciblyConnectTo(patternGraph.getNode(spacialPatternGrid[visited.get(0).getY()][visited.get(0).getX()]), visited.size());
				
			}
			
			

		}
	}
	
	private boolean samePattern(Point a, Point b){
		return spacialPatternGrid[a.getY()][a.getX()] == spacialPatternGrid[b.getY()][b.getX()];
	}
	
	private boolean adjacent(Point a, Point b){
		return a.getX() == b.getX() && Math.abs(a.getY() - b.getY()) == 1 
			|| a.getY() == b.getY() && Math.abs(a.getX() - b.getX()) == 1
			|| Math.abs(a.getY() - b.getY()) == 1 && Math.abs(a.getX() - b.getX()) == 1;
	}
	
	private void assignSpacialPatternsToGrid(){		
		if(micropatterns == null)
			findMicroPatterns();
		
		for(Pattern p : micropatterns){
			if (p instanceof SpacialPattern){
				for(Point point : ((Bitmap)p.getGeometry()).getPoints()){
					spacialPatternGrid[point.getY()][point.getX()] = (SpacialPattern)p;
				}
				
			}
		}
	}
	
//	/**
//	 * Builds the spacial pattern graph.
//	 */
//	private void buildGraph() {
//		List<SpacialPattern> spacials = new ArrayList<SpacialPattern>();
//		java.util.Map<SpacialPattern, Node<SpacialPattern>> nodes = graph.getNodes();
//		Node<SpacialPattern> addedNode = null;
//		
//		for (Pattern pattern : patterns) {
//			if (pattern instanceof SpacialPattern) {
//				spacials.add((SpacialPattern) pattern);
//			}
//		}
//		
//		for (SpacialPattern current : spacials) {
//			addedNode = graph.addNode(current);
//			
//			// Check for adjacent nodes already in the graph. If found,
//			// connect them.
//			Iterator<Entry<SpacialPattern, Node<SpacialPattern>>> it =
//					nodes.entrySet().iterator();
//			while (it.hasNext()) {
//				Entry<SpacialPattern, Node<SpacialPattern>> entry = it.next();
//				
//				// Don't bother if we're looking at the newly added node
//				if (entry.getValue() == addedNode) {
//					break;
//				}
//				
//				SpacialPattern sp = entry.getValue().getValue();
//				
//				if (false) { // TODO: Check for adjacency!
//					addedNode.connectTo(entry.getValue());
//				}
//			}
//		}
//	}

	
	// TODO: Implement this
	/**
	 * Finds macro patterns within a map. It searches for all patterns
	 * available in the finder.patterns.macro package.
	 * 
	 * @return A list of all found pattern instances.
	 */
	public List<CompositePattern> findMacroPatterns() {
		macropatterns = new ArrayList<CompositePattern>();
		
		/*
		 * Do this:
		 * 1. Get all patterns in finder.patterns.meso
		 * 2. Do a pattern search for each pattern for the specified map
		 * 	  using found meso patterns
		 * 3. Save the found patterns
		 * 4. Return all found pattern instances
		 * 
		 * MAYBE: Specify which patterns to look for in the config?
		 */
		if (mesopatterns == null) {
			findMesoPatterns();
		}
		
		return null;
	}
	
	/**
	 * 
	 * @return The pattern graph for this map
	 */
	public Graph<Pattern> getPatternGraph(){
		if(patternGraph == null)
			findMesoPatterns();
		return patternGraph;
	}
}
