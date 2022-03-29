package game.narrative.entity;

import util.Point;

public abstract class Entity {
    Point m_point;

    String m_name = "";
    int m_age = 0;
    String m_backstory = "";

    abstract Point GetPoint();
}
