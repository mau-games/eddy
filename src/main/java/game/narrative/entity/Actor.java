package game.narrative.entity;
import game.narrative.Defines;

public abstract class Actor extends Entity {
    static int actorCounter = 0;

    Defines.Race m_race;
    Defines.Gender m_gender;
    String m_nameSecond = "";
    String m_homeTown = "";
    String m_eyeColour = "";
    String m_skinColour = "";
    String m_hairColour = "";
}
