package game.narrative.entity;

import game.narrative.Defines;
import util.Point;

public class ExtractedGeneratedEntity extends Entity{
    Defines.Gender m_gender = Defines.Gender.LOCKED;
    Defines.Race m_race = Defines.Race.LOCKED;

    public ExtractedGeneratedEntity(String aName, int aAge, int aGenderIndex, int aRaceIndex, String aLikes, String aDislikes, String aAppearance, String aNarrative)
    {
        m_name = aName;
        m_age = aAge;
        m_gender = Defines.Gender.values()[aGenderIndex];
        m_race = Defines.Race.values()[aRaceIndex];
        m_loves = aLikes;
        m_hates = aDislikes;
        m_appearance = aAppearance;
        m_narrative = aNarrative;
    }
    public ExtractedGeneratedEntity(String aName, int aAge, String aGender, String aRace, String aLikes, String aDislikes, String aAppearance, String aNarrative)
    {
        m_name = aName;
        m_age = aAge;
        m_genderStr = aGender;
        m_raceStr = aRace;
        m_loves = aLikes;
        m_hates = aDislikes;
        m_appearance = aAppearance;
        m_narrative = aNarrative;
    }
    public String getURL(){ return ""; }
    public Entity GetEntityType(){ return this;}
    public Point GetPoint(){return null;}
    public void SetID(){}

    public void Print()
    {

        System.out.println("\n----------------------------\n");
        System.out.println("Name: " + m_name);
        System.out.println("Age: " + m_age);
        System.out.println("Gender: " + m_genderStr);
        System.out.println("Race: " + m_raceStr);
        System.out.println("Likes: " + m_loves);
        System.out.println("Dislikes: " + m_hates);
        System.out.println("Appearance: " + m_appearance);
        System.out.println("Narrative: " + m_narrative);
        System.out.println("\n----------------------------\n");
    }
}
