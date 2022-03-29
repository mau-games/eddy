package game.narrative.entity;

import util.Point;

public class Enemy extends Actor{

    public Enemy(Point point)  {
        m_point = point;
    }

    public Point GetPoint(){return m_point;}
}
