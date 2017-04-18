package gui.utils;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import finder.geometry.Bitmap;
import finder.geometry.Geometry;
import finder.geometry.Point;
import finder.geometry.Rectangle;
import finder.graph.Edge;
import finder.graph.Graph;
import finder.graph.Node;
import finder.patterns.InventorialPattern;
import finder.patterns.Pattern;
import finder.patterns.SpacialPattern;
import finder.patterns.meso.GuardRoom;
import finder.patterns.micro.Connector;
import finder.patterns.micro.Corridor;
import finder.patterns.micro.Enemy;
import finder.patterns.micro.Nothing;
import finder.patterns.micro.Room;
import game.TileTypes;
import gui.GUIController;
import javafx.application.Platform;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.MapRendered;

/**
 * This class is used to render maps. The resulting images can be used by
 * e.g. the GUI or by collectors.
 * 
 * @author Johan Holmberg
 */
public class MapRenderer implements Listener {
	
	private static MapRenderer instance = null;
	
	final static Logger logger = LoggerFactory.getLogger(GUIController.class);
	private static EventRouter router = EventRouter.getInstance();
	private ConfigurationUtility config;

	private ArrayList<Image> tiles = new ArrayList<Image>();
	private double patternOpacity = 0;
	private int nbrOfTiles = 6;
	
	private int finalMapWidth;
	private int finalMapHeight;
	
	Dictionary<String, List<Listener>> roster;
	
	private MapRenderer() {
		try {
			config = ConfigurationUtility.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read config: " + e.getMessage());
		}
		
		router.registerListener(this, new AlgorithmDone(null));

		finalMapHeight = config.getInt("map.final_rendition.height");
		finalMapWidth = config.getInt("map.final_rendition.width");
		
		// Set up the tile image list
		for (int i = 0; i < nbrOfTiles; i++) {
			tiles.add(i, null);
		}
	}
	
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return An instance of MapRenderer.
	 */
	public static MapRenderer getInstance() {
		if (instance == null) {
			instance = new MapRenderer();
		}
		return instance;
	}

	@Override
	public void ping(PCGEvent e) {
		if (e instanceof AlgorithmDone) {
			Map result = (Map) ((AlgorithmDone) e).getPayload();
			Platform.runLater(() -> {
				// We might as well see if anyone is interested in our rendered map
				Map map = (Map) e.getPayload();
				sendRenderedMap((game.Map) map.get("map"));
			});
		}
	}

	/**
	 * Draws a matrix onto a graphics ccntext.
	 * 
	 * @param matrix A rectangular matrix of integers. Each integer corresponds
	 * 		to some predefined colour.
	 */
	public synchronized void sketchMap(GraphicsContext ctx, int[][] matrix) {
		ctx.clearRect(0, 0, ctx.getCanvas().getWidth(), ctx.getCanvas().getHeight());
		int m = matrix.length;
		int n = matrix[0].length;
		int pWidth = (int) Math.floor(ctx.getCanvas().getWidth() / Math.max(m, n));

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				ctx.setFill(getColour(matrix[i][j]));
				ctx.fillRect(i * pWidth, j * pWidth, pWidth, pWidth);
			}
		}
	}

	/**
	 * Draws a matrix onto a graphics context.
	 * 
	 * @param ctx The graphics context to draw on.
	 * @param matrix A rectangular matrix of integers. Each integer corresponds
	 * 		to some predefined value.
	 */
	public synchronized void renderMap(GraphicsContext ctx, int[][] matrix) {
		ctx.clearRect(0, 0, ctx.getCanvas().getWidth(), ctx.getCanvas().getHeight());
		int m = matrix.length;
		int n = matrix[0].length;
		int pWidth = (int) Math.floor(ctx.getCanvas().getWidth() / Math.max(m, n));
		Image image = null;

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				image = getTileImage(matrix[i][j]);
				ctx.drawImage(image, i * pWidth, j * pWidth, pWidth, pWidth);
			}
		}
	}
	
	/**
	 * Draws patterns on a map.
	 * 
	 * @param ctx The graphics context to draw on.
	 * @param matrix A rectangular matrix of integers. Each integer corresponds
	 * 		to some predefined value.
	 * @param patterns The patterns to draw.
	 */
	public synchronized void drawPatterns(
			GraphicsContext ctx,
			int[][] matrix,
			Map<Pattern, Color> patterns) {
		
		//TODO: The following calculation should probably be split out into a method
		int m = matrix.length;
		int n = matrix[0].length;
		int pWidth = (int) Math.floor(ctx.getCanvas().getWidth() / Math.max(m, n));
		patternOpacity = config.getDouble("map.pattern_opacity");
				
		for (Entry<Pattern, Color> e : patterns.entrySet()) {
			Platform.runLater(() -> {
				drawPattern(ctx, e.getKey(), e.getValue(), pWidth);
			});
		}
	}
	
	public synchronized void drawGraph(GraphicsContext ctx, int[][] matrix, Graph<Pattern> patternGraph){

		int m = matrix.length;
		int n = matrix[0].length;
		int pWidth = (int) Math.floor(ctx.getCanvas().getWidth() / Math.max(m, n));
		
		patternGraph.resetGraph();
		
		Queue<Node<Pattern>> nodeQueue = new LinkedList<Node<Pattern>>();
		nodeQueue.add(patternGraph.getStartingPoint());
		
		while(!nodeQueue.isEmpty()){
			Node<Pattern> current = nodeQueue.remove();
			current.tryVisit();
			
			
			//Draw the current node
			drawCircle(ctx, getPatternCentre((SpacialPattern)current.getValue(), pWidth),getNodeColor((SpacialPattern)current.getValue()),getNodeRadius((SpacialPattern)current.getValue(),pWidth));
			
			
			
			List<Edge> edges = new ArrayList<Edge>();
			edges.addAll(current.getEdges());
			while(!edges.isEmpty()){
				Edge e = edges.remove(0);
				int edgeCount = 1;
				for(int i = 0; i < edges.size(); i++){
					if(edges.get(i).getNodeA() == e.getNodeA() && edges.get(i).getNodeB() == e.getNodeB()){
						edges.remove(i--);
						edgeCount++;
					}
				}
				ctx.setStroke(Color.BLACK);
				ctx.setLineWidth(e.getWidth()*2);
				
				Point a = getPatternCentre((SpacialPattern)e.getNodeA().getValue(),pWidth);
				Point b = getPatternCentre((SpacialPattern)e.getNodeB().getValue(),pWidth);
				Point half = new Point((a.getX()+b.getX())/2, (a.getY()+b.getY())/2);
				double perpX = b.getY() - a.getY();
				double perpY = a.getX() - b.getX();
				double len = Math.sqrt(perpX*perpX + perpY*perpY);
				perpX /= len;
				perpY /= len;
				
				for(int i = 0; i < edgeCount; i++){
					ctx.beginPath();
					ctx.moveTo(a.getX(), a.getY());
					ctx.quadraticCurveTo(half.getX() + (-20*(edgeCount-1) + 40*i)*perpX, half.getY() + (-20*(edgeCount-1) + 40*i)*perpY, b.getX(), b.getY());
					ctx.stroke();
					ctx.closePath();
				}

				
				//ctx.lineTo(b.getX(), b.getY());
				
				
				
			}
			
			nodeQueue.addAll(current.getEdges().stream().map((Edge<Pattern> e)->{
				Node<Pattern> ret = null;
				if(e.getNodeA() == current){
					ret = e.getNodeB();
				}
				else{
					ret = e.getNodeA();
				}
				//drawLine(ctx,getPatternCentre((SpacialPattern)e.getNodeA().getValue(),pWidth),getPatternCentre((SpacialPattern)e.getNodeB().getValue(),pWidth),Color.BLACK,e.getWidth()*2);
				return ret;
				}).filter((Node<Pattern> node)->{
					if(!node.isVisited()){
						node.tryVisit();
						return true;
					} 
					return false;
				}).collect(Collectors.toList()));
		}
		
		
	}
	
	private Point getPatternCentre(SpacialPattern p, int pWidth){
		Point sum = ((Bitmap)p.getGeometry()).getPoints().stream().reduce(new Point(),(Point result, Point point)->{result.setX(result.getX()+point.getX()); result.setY(result.getY()+point.getY());return result;});
		double x = (double)sum.getX()/((Bitmap)p.getGeometry()).getNumberOfPoints();
		double y = (double)sum.getY()/((Bitmap)p.getGeometry()).getNumberOfPoints();
		return new Point((int)(pWidth*(x+0.5)),(int)(pWidth*(y+0.5)));
	}
	
	private int getNodeRadius(SpacialPattern p, int pWidth){
		if(p instanceof Room)
			return (int)(pWidth * 1.0);
		if(p instanceof Corridor)
			return (int)(pWidth * 0.25);
		if(p instanceof Connector)
			return (int)(pWidth * 0.25);
		if(p instanceof Nothing)
			return (int)(pWidth * 0.25);
		return (int)(pWidth * 2.0);
	}
	
	private Color getNodeColor(SpacialPattern p){
		if(p instanceof Room)
			return Color.BLUE;
		if(p instanceof Corridor)
			return Color.RED;
		if(p instanceof Connector)
			return Color.YELLOW;
		if(p instanceof Nothing)
			return Color.LIGHTGRAY;
		return Color.BLACK;
	}
	
	/**
	 * Publishes a rendered map.
	 */
	private void sendRenderedMap(game.Map map) {
		finalMapHeight = config.getInt("map.final_rendition.height");
		finalMapWidth = config.getInt("map.final_rendition.width");
		Canvas canvas = new Canvas(finalMapWidth, finalMapHeight);
		renderMap(canvas.getGraphicsContext2D(), map.toMatrix());
		Image image = canvas.snapshot(new SnapshotParameters(), null);
		router.postEvent(new MapRendered(image));
	}

	/**
	 * Selects a colour based on the pixel's integer value.
	 * 
	 * @param pixel The pixel to select for.
	 * @return A selected colour code.
	 */
	private Color getColour(int pixel) {
		Color color = null;

		switch (TileTypes.toTileType(pixel)) {
		case DOOR:
			color = Color.BLACK;
			break;
		case TREASURE:
			color = Color.YELLOW;
			break;
		case ENEMY:
			color = Color.RED;
			break;
		case WALL:
			color = Color.DARKSLATEGRAY;
			break;
		case FLOOR:
			color = Color.WHITE;
			break;
		case DOORENTER:
			color = Color.MAGENTA;
			break;
		default:
			color = Color.WHITE;
		}

		return color;
	}

	/**
	 * Selects a tile image based on the pixel's integer value.
	 * 
	 * @param pixel The pixel to select for.
	 * @return A file.
	 */
	private Image getTileImage(int pixel) {
		Image image = tiles.get(pixel);

		if (image == null) {
			switch (TileTypes.toTileType(pixel)) {
			case DOOR:
				image = new Image("/" + config.getString("map.tiles.door"));
				break;
			case TREASURE:
				image = new Image("/" + config.getString("map.tiles.treasure"));
				break;
			case ENEMY:
				image = new Image("/" + config.getString("map.tiles.enemy"));;
				break;
			case WALL:
				image = new Image("/" + config.getString("map.tiles.wall"));;
				break;
			case FLOOR:
				image = new Image("/" + config.getString("map.tiles.floor"));;
				break;
			case DOORENTER:
				image = new Image("/" + config.getString("map.tiles.doorenter"));;
				break;
			default:
				image = null;
			}
			tiles.set(pixel, image);
		}

		return image;
	}
	
	/**
	 * Outlines a pattern onto the map.
	 * 
	 * @param ctx The graphics context to draw on.
	 * @param p The pattern.
	 * @param c The colour to use.
	 * @param pWidth The width of a "pixel".
	 */
	private void drawPattern(
			GraphicsContext ctx,
			Pattern p, Color c,
			int pWidth) {
		Geometry g = p.getGeometry();
		
		if (g instanceof Point) {
			drawPoint(ctx, (Point) g, c, pWidth);
		} else if (g instanceof Bitmap) {
			for (Point point : ((finder.geometry.Polygon) g).getPoints()) {
				drawPoint(ctx, point, c, pWidth);
			}
		} else if (g instanceof finder.geometry.Rectangle) {
			drawRectangle(ctx, (Rectangle) g, c, pWidth);
		}
	}
	
	/**
	 * Draws a point on the map
	 * 
	 * @param ctx The graphics context to draw on.
	 * @param p The point to outline.
	 * @param c The colour to use.
	 * @param width The width of a "pixel".
	 */
	private void drawPoint(GraphicsContext ctx, Point p, Color c, int width) {
		ctx.setFill(new Color(c.getRed(), c.getGreen(), c.getBlue(), patternOpacity));
		ctx.setStroke(c);
		ctx.setLineWidth(2);
		ctx.fillRect(p.getX() * width, p.getY() * width, width - 1, width - 1);
		ctx.strokeRect(p.getX() * width, p.getY() * width, width - 1, width - 1);
	}
	
	/**
	 * Draws a rectangle on the map.
	 * 
	 * @param ctx The graphics context to draw on.
	 * @param r The rectangle to outline.
	 * @param c The colour to use.
	 * @param x The x value of the first point.
	 * @param y The y value of the first point.
	 * @param pWidth The width of a "pixel".
	 */
	private void drawRectangle(GraphicsContext ctx, Rectangle r, Color c, int pWidth) {
		int x = r.getTopLeft().getX() * pWidth;
		int y = r.getTopLeft().getY() * pWidth;
		int width = (r.getBottomRight().getX() - x + 1) * pWidth + pWidth - 1;
		int height = (r.getBottomRight().getY() - y + 1) * width + width - 1;
		
		ctx.setFill(new Color(c.getRed(), c.getGreen(), c.getBlue(), patternOpacity));
		ctx.setStroke(c);
		ctx.setLineWidth(2);
		ctx.fillRect(x, y, width, height);
		ctx.strokeRect(x, y, width, height);
	}
	
	private void drawCircle(GraphicsContext ctx, Point p, Color c, int radius){
		ctx.setFill(new Color(c.getRed(), c.getGreen(), c.getBlue(), 0.8));
		ctx.setStroke(c);
		ctx.setLineWidth(4);
		ctx.fillOval(p.getX()-radius, p.getY()-radius, 2*radius, 2*radius);
		ctx.strokeOval(p.getX()-radius, p.getY()-radius, 2*radius, 2*radius);
	}
	
	private void drawLine(GraphicsContext ctx, Point a, Point b, Color c, double width){
		ctx.setStroke(c);
		ctx.setLineWidth(width);
		ctx.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
	}
}
