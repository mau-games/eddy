package game.narrative.entity;
import game.narrative.Defines;
import util.Point;

public class Item extends Entity {
    Defines.ItemType m_itemType;
    Defines.Element m_element;

    public Item(Point point)  {
        m_point = point;
    }

    public Point GetPoint(){return m_point;}
    public void SetElement(Defines.Element value) {m_element = value;}
    public void SetItemType (Defines.ItemType value) { m_itemType = value;}
}
