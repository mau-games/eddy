package game.narrative.entity;
import game.narrative.Defines;
import util.Point;

public class Item extends Entity {
    static int itemCounter = 0;

    private String imageURL = "@../../graphics/tiles/item.png";
    public String GetEntityImage(){ return imageURL;}
    public String getURL(){ return imageURL;};

    Defines.ItemType m_itemType;
    Defines.Element m_element;

    public Item(Point point)  {
        m_point = point;
        SetID();
    }

    public Entity GetEntityType(){ return this;}

    public Point GetPoint(){return m_point;}
    public void SetElement(Defines.Element value) {m_element = value;}
    public void SetItemType (Defines.ItemType value) { m_itemType = value;}

    public void SetID(){
        m_Entity_ID = this.getClass().getSimpleName() + String.valueOf(itemCounter++);
    }
}
