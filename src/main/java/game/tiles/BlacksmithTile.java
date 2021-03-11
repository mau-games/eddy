package game.tiles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import finder.geometry.Point;
import game.Tile;
import game.TileTypes;
import generator.algorithm.grammar.QuestGrammar.QuestMotives;
import gui.controls.Brush;

public class BlacksmithTile extends NpcTile{
	List<QuestMotives> questMotiveList;
	
	
	public BlacksmithTile()
    {
        m_type = TileTypes.BLACKSMITH;
        setBrushUsage();
        AddQuestMotives();
    }

    public BlacksmithTile(Point p, TileTypes type)
    {
        super(p, type);
        setBrushUsage();
        AddQuestMotives();
    }

    public BlacksmithTile(int x, int y, TileTypes type)
    {
        super(x, y, type);
        setBrushUsage();
        AddQuestMotives();
    }

    public BlacksmithTile(Point p, int typeValue)
    {
        super(p, typeValue);
        setBrushUsage();
        AddQuestMotives();
    }

    public BlacksmithTile(int x, int y, int typeValue)
    {
        super(x, y, typeValue);
        setBrushUsage();
        AddQuestMotives();
    }

    public BlacksmithTile(Tile copyTile)
    {
        super(copyTile);
        m_type = TileTypes.BLACKSMITH;
        setBrushUsage();
        AddQuestMotives();
    }

//    @Override
//    public void PaintTile(Point currentCenter, Room room, Drawer drawer, InteractiveMap interactiveCanvas)
//    {
//        interactiveCanvas.getCell(currentCenter.getX(), currentCenter.getY()).
//                setImage(interactiveCanvas.getImage(m_type, interactiveCanvas.scale));
//    }

    @Override
    public Brush modification(Brush brush)
    {
        brush.setImmutable(true);
        return brush;
    }
    @Override
    public List<QuestMotives> ReturnMotives()
    {
    	return questMotiveList;
    }
    @Override
    public boolean CheckMotives(QuestMotives temp)
    {
    	for (int i = 0; i < questMotiveList.size(); i++) {
			if (questMotiveList.get(i) == temp) {
				return true;
			}
		}
    	return false;
    }
    
    private void AddQuestMotives()
    {
        questMotiveList = new ArrayList<QuestMotives>();
        questMotiveList.add(QuestMotives.KNOWLEDGE);
        questMotiveList.add(QuestMotives.EQUIPMENT);
        questMotiveList.add(QuestMotives.WEALTH);
    }

//    @Override
//    protected void setBrushUsage()
//    {
//        super.setBrushUsage();
//        SetImmutable(true);
//    }
//
//    @Override
//    public Tile copy()
//    {
//        return new NpcTile(this);
//    }
}
