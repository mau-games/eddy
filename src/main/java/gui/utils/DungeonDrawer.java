package gui.utils;

public class DungeonDrawer 
{
	private static DungeonDrawer instance = null;
	
	protected ShapeBrush brush;
//	protected List<ShapeBrush> allBrushes
	
	private DungeonDrawer()
	{
		brush = new MoveElementBrush();
	}
	
	public static DungeonDrawer getInstance()
	{
		if(instance == null)
		{
			instance = new DungeonDrawer();
		}
		
		return instance;
	}
	
	public void changeToConnector()
	{
		brush = new RoomConnector();
	}
	
	public void changeToMove()
	{
		brush = new MoveElementBrush();
	}
	
	public ShapeBrush getBrush()
	{
		return brush;
	}
}
