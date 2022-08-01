package game.narrative.NarrativeFinder;

import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;

import java.util.ArrayList;
import java.util.List;

/**
 * PatternFinder is used to find patterns within a room.
 * 
 * @author Alberto Alvarez
 */
public class NarrativeStructPatternFinder {

	private GrammarGraph narrative_graph;
	public ArrayList<NarrativePattern> all_narrative_patterns;
	private List<StructureNodePattern> structure_nodes;
	private List<Structure> structures; //this is going to be a composite structure (a set of subgraphs)
	private List<PlotPoint> plot_points;
	private List<PlotDevicePattern> plot_devices;

	//This pattern graph is basically the same as the graph itself.
//	private Graph<Pattern> patternGraph = null;
//	private SpacialPattern[][] spacialPatternGrid = null;

	/**
	 * Creates a pattern finder instance.

	 */
	public NarrativeStructPatternFinder(GrammarGraph narrative_graph) {
		this.narrative_graph = narrative_graph;
		this.all_narrative_patterns = new ArrayList<NarrativePattern>();
	}

//	public List<NarrativePattern> getAllInstances(GrammarNode gn)
//	{

	/**
	 * Helper method to check if a node has been categorized as a pattern
	 * @param gn
	 * @return
	 */
	public NarrativePattern existNodeAsPattern(GrammarNode gn)
	{
		for(NarrativePattern np : all_narrative_patterns)
		{
			if(np.connected_node != null && np.connected_node == gn)
				return np;
		}

		return null;
	}

	/**
	 * Helper method to get all instances that have this node as pattern
	 * @param gn
	 * @return
	 */
	public ArrayList<NarrativePattern> getAllInstances(GrammarNode gn)
	{
		ArrayList<NarrativePattern> nps = new ArrayList<NarrativePattern>();

		for(NarrativePattern np : all_narrative_patterns)
		{
			if(np.connected_node != null && np.connected_node == gn)
			{
				nps.add(np);
				continue;
			}

			if(np instanceof CompositeNarrativePattern && ((CompositeNarrativePattern) np).relevant_nodes.contains(gn))
			{
				nps.add(np);
			}
		}

		return nps;
	}
	/**
	 * Helper method to get all micro-patterns in the graph
	 * @return all micro-patterns in the graph
	 */
	public ArrayList<NarrativePattern> getAllMicros()
	{
		ArrayList<NarrativePattern> nps = new ArrayList<NarrativePattern>();

		for(NarrativePattern np : all_narrative_patterns)
		{
			if(np instanceof HeroNodePattern || np instanceof VillainNodePattern ||
					np instanceof StructureNodePattern || np instanceof PlotDevicePattern)
			{
				nps.add(np);
			}
		}

		return nps;
	}


	/**
	 * Feels a bit incorrect to get everything like this.
	 * @param fType
	 * @param <T>
	 * @return
	 */
	public <T extends NarrativePattern> ArrayList<T> getAllPatternsByType(Class<T> fType) {
		ArrayList<T> list = new ArrayList<T>();
		for (NarrativePattern np : all_narrative_patterns) {
			if (np.getClass() ==  fType) {
				list.add(fType.cast(np));
			}
		}
		return list;
	}

	public ArrayList<NarrativePattern> findNarrativePatterns(GrammarGraph axiom)
	{
		/***
		 * We need to collect all the possible patterns within a graph.
		 * 1- perhaps the best would be to first find the structure nodes (should be as simple as collecting the type of node)
		 * 2- Then we could aim at getting all the structures (narrative structures) that exist within the graph
		 * 	(without these structures, the graph really does not say anything --> CONFLICT is central.
		 * 3- Get the plot points. Should be "easy" if we know the structures as thy will be connected among them
		 * 4- Find the plot devices. Again, should be "simple" as there are nodes already categorized as Plot devices.
		 * 5- Perhaps it would be a good time to find the tension (how to calculate??) --> Probably I need to read a bit!
		 * Other patterns that might be relevant!
		 * 		- Goals
		 * 		- Tasks
		 * 		- Obstacles
		 * 		- Side/main goals (better related as kernels and satelites) - although, this is more for quests
		 * 			- Side goals I can only see it as when adding plot devices faktist.
		 */

		if(!all_narrative_patterns.isEmpty())
			return all_narrative_patterns;

		all_narrative_patterns = new ArrayList<NarrativePattern>();
		structure_nodes = new ArrayList<StructureNodePattern>();

		//First, find all the Basic Narrative Patterns (i.e., micro-patterns)
		all_narrative_patterns.addAll(StructureNodePattern.matches(narrative_graph));
		all_narrative_patterns.addAll(HeroNodePattern.matches(narrative_graph));
		all_narrative_patterns.addAll(VillainNodePattern.matches(narrative_graph));
		all_narrative_patterns.addAll(PlotDevicePattern.matches(narrative_graph));

		//Now get all the connections of basic patterns!
		int basic_counter = 0;
		for(NarrativePattern np : all_narrative_patterns)
		{

			if(np instanceof BasicNarrativePattern)
			{
				((BasicNarrativePattern) np).storeAllConnections(narrative_graph, this);
				basic_counter++;
			}

		}

		System.out.println("micropat: " + basic_counter);

		// Now lets start getting the composite ones! (i.e., meso-patterns)
		all_narrative_patterns.addAll(SimpleConflictPattern.matches(narrative_graph, all_narrative_patterns, this));

		all_narrative_patterns.addAll(ActivePlotDevice.matches(narrative_graph, all_narrative_patterns, this));


		all_narrative_patterns.addAll(DerivativePattern.matches(narrative_graph, all_narrative_patterns, this));
		all_narrative_patterns.addAll(RevealPattern.matches(narrative_graph, all_narrative_patterns, this));
		all_narrative_patterns.addAll(ImplicitConflictPattern.matches(narrative_graph, all_narrative_patterns, this));

		//Now lets go with the plot points
		all_narrative_patterns.addAll(PlotPoint.matches(narrative_graph, all_narrative_patterns, this));
		all_narrative_patterns.addAll(PlotTwist.matches(narrative_graph, all_narrative_patterns, this));

		//Finally get the nothing!
		all_narrative_patterns.addAll(NothingNarrativePattern.matches(narrative_graph, all_narrative_patterns, this));
		all_narrative_patterns.addAll(BrokenLinkPattern.matches(narrative_graph, all_narrative_patterns, this));



//		all_narrative_patterns.addAll(CompoundConflictPattern.matches(narrative_graph, all_narrative_patterns));
//		all_narrative_patterns.addAll(PlotPoint.matches(narrative_graph));
//		all_narrative_patterns.addAll(PlotDevice.matches(narrative_graph));

		structures = new ArrayList<Structure>();
//		all_narrative_patterns.addAll(Structure.matches(narrative_graph));

		plot_points = new ArrayList<PlotPoint>();
//		all_narrative_patterns.addAll(PlotPoint.matches(narrative_graph));

		plot_devices = new ArrayList<PlotDevicePattern>();
//		all_narrative_patterns.addAll(PlotDevice.matches(narrative_graph));

		for(NarrativePattern np : all_narrative_patterns)
		{
//			np.calculateQuality(all_narrative_patterns, this);
			np.calculateTropeQuality(null, narrative_graph, axiom, all_narrative_patterns, this);
//			System.out.println("Pattern: " + np.getClass());
//			if(np.connected_node != null)
//				System.out.println("Type: " + np.connected_node.toString());
//			System.out.println("Quality: " + np.quality);
//			System.out.println();
		}


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
//		micropatterns.addAll(Corridor.matches(room, null)); // This also finds connectors
//		micropatterns.addAll(Chamber.matches(room, null));
//		micropatterns.addAll(Treasure.matches(room, null));
//		micropatterns.addAll(Enemy.matches(room, null));
//		micropatterns.addAll(Boss.matches(room, null)); //NEWLY ADDED
//		micropatterns.addAll(Door.matches(room, null));
//		micropatterns.addAll(Nothing.matches(room, null)); // This MUST come last
//
//		return micropatterns;

		int allpats = all_narrative_patterns.size() - getAllPatternsByType(CompoundConflictPattern.class).size();
		int all_pats_no_implicit_conflict = all_narrative_patterns.size() - getAllPatternsByType(ImplicitConflictPattern.class).size();
		int combined_all_pats = all_narrative_patterns.size() -
				getAllPatternsByType(CompoundConflictPattern.class).size() -
				getAllPatternsByType(ImplicitConflictPattern.class).size();
		int fake_conflicts = 0;

		//ArrayList<ImplicitConflictPattern> nps = getAllPatternsByType(ImplicitConflictPattern.class);

		for(NarrativePattern np : all_narrative_patterns)
		{

			if(np instanceof SimpleConflictPattern)
			{
				if(((SimpleConflictPattern) np).fake_conflict)
					fake_conflicts++;
			}

		}

		System.out.println("all: " + all_narrative_patterns.size() + ", without compounds: " + allpats +
				", without implicit confls.: " + all_pats_no_implicit_conflict +
				", without combined_confls.: " + combined_all_pats +
				", conflicts: " + getAllPatternsByType(SimpleConflictPattern.class).size() +
				", fake conflicts: " + fake_conflicts +
				", derivatives: " + getAllPatternsByType(DerivativePattern.class).size() +
				", reveals: " + getAllPatternsByType(RevealPattern.class).size() +
				", APD: " + getAllPatternsByType(ActivePlotDevice.class).size() +
				", PP: " + getAllPatternsByType(PlotPoint.class).size() +
				", PT: " + getAllPatternsByType(PlotTwist.class).size());
		return all_narrative_patterns;
	}
//
//	// TODO: Implement this
//	/**
//	 * Finds micro patterns within a map. It searches for all patterns
//	 * available in the finder.patterns.micro package.
//	 *
//	 * @return A list of all found pattern instances.
//	 */
//	public List<Pattern> findMicroPatterns() {
//		if (micropatterns != null) {
//			return micropatterns;
//		}
//
//		micropatterns = new ArrayList<Pattern>();
//
//		/*
//		 * Do this:
//		 * 1. Get all patterns in finder.patterns.micro
//		 * 2. Do a pattern search for each pattern for the specified map
//		 * 3. Save the found patterns
//		 * 4. Return all found pattern instances
//		 *
//		 * MAYBE: Specify which patterns to look for in the config?
//		 *
//		 * For now, let's just implicitly call each pattern.
//		 */
//		micropatterns.addAll(Corridor.matches(room, null)); // This also finds connectors
//		micropatterns.addAll(Chamber.matches(room, null));
//		micropatterns.addAll(Treasure.matches(room, null));
//		micropatterns.addAll(Enemy.matches(room, null));
//		micropatterns.addAll(Boss.matches(room, null)); //NEWLY ADDED
//		micropatterns.addAll(Door.matches(room, null));
//		micropatterns.addAll(Nothing.matches(room, null)); // This MUST come last
//
//		return micropatterns;
//	}
//
//	// TODO: Implement this
//	/**
//	 * Finds meso patterns within a map. It searches for all patterns available
//	 * in the finder.patterns.meso package.
//	 *
//	 * @return A list of all found pattern instances.
//	 */
//	public List<CompositePattern> findMesoPatterns() {
//		if(mesopatterns != null){
//			return mesopatterns;
//		}
//		mesopatterns = new ArrayList<CompositePattern>();
//
//		/*
//		 * Do this:
//		 * 1. Get all patterns in finder.patterns.meso
//		 * 2. Do a pattern search for each pattern for the specified map
//		 * 	  using found micro patterns
//		 * 3. Save the found patterns
//		 * 4. Return all found pattern instances
//		 *
//		 * MAYBE: Specify which patterns to look for in the config?
//		 */
//		if (micropatterns == null) {
//			findMicroPatterns();
//		}
//
//		Populator.populate(micropatterns);
//		buildPatternGraph();
//
//		//TODO: We need to fix this for the new way we are handling this
//		mesopatterns.addAll(ChokePoint.matches(room, patternGraph, mesopatterns));
//		mesopatterns.addAll(DeadEnd.matches(room, patternGraph, mesopatterns));
//		mesopatterns.addAll(Ambush.matches(room, patternGraph, mesopatterns));
//		mesopatterns.addAll(GuardRoom.matches(room, patternGraph, mesopatterns));
//		mesopatterns.addAll(TreasureRoom.matches(room, patternGraph, mesopatterns));
//		mesopatterns.addAll(GuardedTreasure.matches(room, patternGraph, mesopatterns)); //Do this last!
//		return mesopatterns;
//	}
//
//	public List<CompositePattern> getMesoPatterns(){
//		return findMesoPatterns();
//	}
//
//	public List<Pattern> getMicroPatterns(){
//		return findMicroPatterns();
//	}
//
//	public List<CompositePattern> getMacroPatterns() {
//		return findMacroPatterns();
//	}
//
//	private void buildPatternGraph(){
//		//Build the pattern graph
//		assignSpacialPatternsToGrid();
//
//		//TODO: THIS STILL NEEDS A LOT OF WORK
//
//		boolean visitedTiles[][] = new boolean[room.getRowCount()][room.getColCount()];
//
//		patternGraph = new Graph<Pattern>();
//
////		Node<Pattern> start = patternGraph.addNode(spacialPatternGrid[entrancePosition.getY()][entrancePosition.getX()]);
//
//		//Do a flood fill from this pattern to find all patterns
//		Queue<Node<Pattern>> patternQueue = new LinkedList<Node<Pattern>>();
//
////		if(start.getValue() != null)
////		patternQueue.add(start);
//
//		for(Pattern microPattern : micropatterns)
//		{
//			if(microPattern instanceof Door)
//			{
//				Point doorPos = (Point)microPattern.getGeometry();
//				if(!patternGraph.containsNode(spacialPatternGrid[doorPos.getY()][doorPos.getX()]))
//				{
//					patternQueue.add(patternGraph.addNode(spacialPatternGrid[doorPos.getY()][doorPos.getX()]));
//				}
//			}
//		}
//
//
//
//		while(!patternQueue.isEmpty()){
//			//TODO: PROBLEM IS HERE
//			Node<Pattern> currentPattern = patternQueue.remove();
//			//currentPattern.tryVisit();
//
//			//Do a flood fill from a point in this pattern to find adjacent patterns
//			Queue<Point> tileQueue = new LinkedList<Point>();
//
//			tileQueue.add(((Bitmap)currentPattern.getValue().getGeometry()).getPoint(0));
//			visitedTiles[((Bitmap)currentPattern.getValue().getGeometry()).getPoint(0).getY()][((Bitmap)currentPattern.getValue().getGeometry()).getPoint(0).getX()] = true;
//
//			List<Point> adjacentTiles = new ArrayList<Point>();
//
//			//After this loop, adjacentTiles should contain all of the pattern tiles adjacent to this pattern
//			while(!tileQueue.isEmpty()){
//
//				Point currentPoint = tileQueue.remove();
//				int x = currentPoint.getX();
//				int y = currentPoint.getY();
//
//				//Add neighbouring tiles
//				if(x > 0 && !visitedTiles[y][x-1]){
//					if(spacialPatternGrid[y][x-1] == currentPattern.getValue()){
//						tileQueue.add(new Point(x-1,y));
//						visitedTiles[y][x-1] = true;
//					} else if(spacialPatternGrid[y][x-1] != null){
//						Point adjacentPoint = new Point(x-1,y);
//						if(adjacentTiles.stream().noneMatch((Point p) -> {return p.equals(adjacentPoint);})){
//							adjacentTiles.add(adjacentPoint);
//						}
//					}
//				}
//				if(x < room.getColCount() - 1 && !visitedTiles[y][x+1]){
//					if(spacialPatternGrid[y][x+1] == currentPattern.getValue()){
//						tileQueue.add(new Point(x+1,y));
//						visitedTiles[y][x+1] = true;
//					} else if (spacialPatternGrid[y][x+1] != null){
//						Point adjacentPoint = new Point(x+1,y);
//						if(adjacentTiles.stream().noneMatch((Point p) -> {return p.equals(adjacentPoint);})){
//							adjacentTiles.add(adjacentPoint);
//						}
//					}
//				}
//				if(y > 0 && !visitedTiles[y-1][x]){
//					if(spacialPatternGrid[y-1][x] == currentPattern.getValue()){
//						tileQueue.add(new Point(x,y-1));
//						visitedTiles[y-1][x] = true;
//					} else if (spacialPatternGrid[y-1][x] != null){
//						Point adjacentPoint = new Point(x,y-1);
//						if(adjacentTiles.stream().noneMatch((Point p) -> {return p.equals(adjacentPoint);})){
//							adjacentTiles.add(adjacentPoint);
//						}
//					}
//				}
//				if(y < room.getRowCount() - 1 && !visitedTiles[y+1][x]){
//					if(spacialPatternGrid[y+1][x] == currentPattern.getValue()){
//						tileQueue.add(new Point(x,y+1));
//						visitedTiles[y+1][x] = true;
//					} else if (spacialPatternGrid[y+1][x] != null){
//						Point adjacentPoint = new Point(x,y+1);
//						if(adjacentTiles.stream().noneMatch((Point p) -> {return p.equals(adjacentPoint);})){
//							adjacentTiles.add(adjacentPoint);
//						}
//					}
//				}
//
//			}
//
//			//Create the new nodes
//			for(Point p : adjacentTiles){
//				Node<Pattern> n = patternGraph.addNode(spacialPatternGrid[p.getY()][p.getX()]);
//				if(n != null){
//					patternQueue.add(n);
//				}
//			}
//
//			//For each adjacent pattern, determine the number an "width" of edges and add to the graph.
//			while(!adjacentTiles.isEmpty()){
//				List<Point> visited = new ArrayList<Point>();
//				Queue<Point> adjacencyQueue = new LinkedList<Point>();
//				adjacencyQueue.add(adjacentTiles.remove(0));
//
//				while(!adjacencyQueue.isEmpty()){
//					Point currentPoint = adjacencyQueue.remove();
//					visited.add(currentPoint);
//					adjacencyQueue.addAll(adjacentTiles.stream().filter((Point p)->{return adjacent(currentPoint,p) && samePattern(currentPoint,p);}).collect(Collectors.toList()));
//					adjacentTiles.removeIf((Point p)->{return adjacent(currentPoint,p) && samePattern(currentPoint,p);});
//				}
//
//				currentPattern.forciblyConnectTo(patternGraph.getNode(spacialPatternGrid[visited.get(0).getY()][visited.get(0).getX()]), visited.size());
//
//			}
//
//
//
//		}
//	}
//
//	private boolean samePattern(Point a, Point b){
//		return spacialPatternGrid[a.getY()][a.getX()] == spacialPatternGrid[b.getY()][b.getX()];
//	}
//
//	private boolean adjacent(Point a, Point b){
//		return a.getX() == b.getX() && Math.abs(a.getY() - b.getY()) == 1
//			|| a.getY() == b.getY() && Math.abs(a.getX() - b.getX()) == 1
//			|| Math.abs(a.getY() - b.getY()) == 1 && Math.abs(a.getX() - b.getX()) == 1;
//	}
//
//	private void assignSpacialPatternsToGrid(){
//		if(micropatterns == null)
//			findMicroPatterns();
//
//		for(Pattern p : micropatterns){
//			if (p instanceof SpacialPattern){
//				for(Point point : ((Bitmap)p.getGeometry()).getPoints()){
//					spacialPatternGrid[point.getY()][point.getX()] = (SpacialPattern)p;
//				}
//
//			}
//		}
//	}
//
////	/**
////	 * Builds the spacial pattern graph.
////	 */
////	private void buildGraph() {
////		List<SpacialPattern> spacials = new ArrayList<SpacialPattern>();
////		java.util.Map<SpacialPattern, Node<SpacialPattern>> nodes = graph.getNodes();
////		Node<SpacialPattern> addedNode = null;
////
////		for (Pattern pattern : patterns) {
////			if (pattern instanceof SpacialPattern) {
////				spacials.add((SpacialPattern) pattern);
////			}
////		}
////
////		for (SpacialPattern current : spacials) {
////			addedNode = graph.addNode(current);
////
////			// Check for adjacent nodes already in the graph. If found,
////			// connect them.
////			Iterator<Entry<SpacialPattern, Node<SpacialPattern>>> it =
////					nodes.entrySet().iterator();
////			while (it.hasNext()) {
////				Entry<SpacialPattern, Node<SpacialPattern>> entry = it.next();
////
////				// Don't bother if we're looking at the newly added node
////				if (entry.getValue() == addedNode) {
////					break;
////				}
////
////				SpacialPattern sp = entry.getValue().getValue();
////
////				if (false) { // TODO: Check for adjacency!
////					addedNode.connectTo(entry.getValue());
////				}
////			}
////		}
////	}
//
//
//	// TODO: Implement this
//	/**
//	 * Finds macro patterns within a map. It searches for all patterns
//	 * available in the finder.patterns.macro package.
//	 *
//	 * @return A list of all found pattern instances.
//	 */
//
//	public List<CompositePattern> findMacroPatterns() {
//		if (macropatterns != null) {
//			return macropatterns;
//		}
//
//		macropatterns = new ArrayList<CompositePattern>();
//
//		/*
//		 * Do this:
//		 * 1. Get all patterns in finder.patterns.meso
//		 * 2. Do a pattern search for each pattern for the specified map
//		 * 	  using found meso patterns
//		 * 3. Save the found patterns
//		 * 4. Return all found pattern instances
//		 *
//		 * MAYBE: Specify which patterns to look for in the config?
//		 */
//		if (mesopatterns == null) {
//			findMesoPatterns();
//		}
//
//		DefeatEnemies de = DefeatEnemies.getBestDefeatEnemies(mesopatterns);
//		if (de != null)
//			macropatterns.add(de);
//
//		DefeatBoss db = DefeatBoss.getBestDefeatBoss(mesopatterns);
//		if (db != null)
//			macropatterns.add(db);
//
//		FindTreasure ft = FindTreasure.getBestFindTreasure(mesopatterns);
//		if (ft != null)
//			macropatterns.add(ft);
//
//		return macropatterns;
//	}
//
//	/**
//	 *
//	 * @return The pattern graph for this map
//	 */
//	public Graph<Pattern> getPatternGraph(){
//		if(patternGraph == null)
//			findMesoPatterns();
//		return patternGraph;
//	}
}
