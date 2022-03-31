package game.narrative.entity;
import game.TileTypes;
import game.narrative.Defines;
import util.Point;

public class NPC extends Actor {
    private String imageURL = "@../../graphics/tiles/NPC.png";
    //public String GetEntityImage(){ return imageURL;}
    public String getURL(){ return imageURL;};
    private TileTypes tileType;
    Defines.Class m_class;

    public NPC(Point point , TileTypes type)  {
        m_point = point;
        tileType = type;
        SetImageURL();
        SetID();
    }
    public Entity GetEntityType(){ return this;}

    private void SetImageURL(){
        switch (tileType){
            case SOLDIER:
                imageURL = "@../../graphics/tiles/KnightTile.png";
                break;
            case MAGE:
                imageURL = "@../../graphics/tiles/WizardTile.png";
                break;
            case BOUNTYHUNTER:
                imageURL = "@../../graphics/tiles/BountyHunterTile.png";
                break;
            case CIVILIAN:
                imageURL = "@../../graphics/tiles/MerchantTile.png";
                break;
            default:
                imageURL = "@../../graphics/tiles/NPC.png";
                break;
        }
    }
    public Point GetPoint(){return m_point;}

    public void SetID(){
        m_Entity_ID = this.getClass().getSimpleName() + String.valueOf(actorCounter++);
    }
/*    public void SetName(String input){
        m_name = input;
    }*/
}
