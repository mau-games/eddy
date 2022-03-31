package game.narrative.entity;

import util.Point;

public class Enemy extends Actor{
    private String imageURL = "@../../graphics/tiles/enemy.png";
    public String getURL(){ return imageURL; };


    public Enemy(Point point)  {
        m_point = point;
        SetID();
    }
    public Entity GetEntityType(){ return this;}

    public Point GetPoint(){return m_point;}

    public void SetID(){
        m_Entity_ID = this.getClass().getSimpleName() + String.valueOf(actorCounter++);
    }
}
