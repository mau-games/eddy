package game.quest;

import finder.geometry.Point;
import game.Room;

import java.util.UUID;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public abstract class Action {
    private UUID id;
    private boolean precondition;
    private ActionType type;
    private Room room;
    private Point position;
    private String name;

    public Action() {
        precondition = true;
    }

    public Action(boolean precondition) {
        this.precondition = precondition;
    }

    public Action(ActionType type) {
        this.type = type;
    }

    public Action(int typeValue){
        this.type = ActionType.toActionType(typeValue);
    }

    public Action(boolean precondition, ActionType type) {
        this.precondition = precondition;
        this.type = type;
    }

    public Action(Action action) {
        precondition = action.isPreconditionMet();
        type = action.getType();
        position = action.getPosition();
        name = action.getName();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return type.toString();
    }

    public boolean isPreconditionMet() {
        return precondition;
    }

    public void setPrecondition(boolean precondition) {
        this.precondition = precondition;
    }

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public void setPosition(int x, int y){
        this.position.setX(x);
        this.position.setY(y);
    }

//    public abstract Action copy();
//    {
//        return new Action(this);
//    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public abstract void checkConditions();
}
