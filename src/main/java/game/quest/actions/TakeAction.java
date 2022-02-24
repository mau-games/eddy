package game.quest.actions;

import java.util.ArrayList;
import java.util.List;

import finder.geometry.Point;
import game.Room;
import game.Tile;
import game.quest.Action;
import game.quest.ActionType;
import game.quest.ActionWithSecondPosition;
import generator.algorithm.grammar.QuestGrammar.QuestMotives;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class TakeAction extends ActionWithSecondPosition {
    private Point secondPosition;
    private Room secondRoom;
    public TakeAction() {
        this.setType(ActionType.TAKE);

    }

    public TakeAction(boolean precondition) {
        super(precondition);
    }

    public TakeAction(ActionType type) {
        super(type);
    }

    public Point getSecondPosition() {
        return secondPosition;
    }

    public void setSecondPosition(Point secondPosition) {
        this.secondPosition = secondPosition;
    }

    @Override
    public Room getSecondRoom() {
        return secondRoom;
    }

    @Override
    public void setSecondRoom(Room room) {
        this.secondRoom = room;
    }

    @Override
    public void checkConditions() {
        Tile tile = getRoom().getTile(getPosition().getX(),getPosition().getY());
        Tile secondTile = getSecondRoom().getTile(getSecondPosition().getX(),getSecondPosition().getY());
        setPrecondition(
                tile.GetType().isItem() ||
                        secondTile.GetType().isCivilian());
    }
}
