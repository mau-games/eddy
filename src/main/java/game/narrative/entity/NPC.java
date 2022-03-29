package game.narrative.entity;
import game.narrative.Defines;
import util.Point;

public class NPC extends Actor {
    Defines.Class m_class;

    public NPC(Point point)  {
        m_point = point;
    }

    public Point GetPoint(){return m_point;}
}
