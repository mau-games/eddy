package game.narrative.entity;

import game.narrative.Defines;

public class ExtractedEntityData {
    String m_name = "";
    int m_age = 0;
    Defines.Gender m_gender = Defines.Gender.LOCKED;
    String m_genderStr = "";
    Defines.Race m_race = Defines.Race.LOCKED;
    String m_raceStr = "";
    Defines.Class m_class = Defines.Class.LOCKED;
    String m_classStr = "";
    String m_loves = "";
    String m_hates = "";
    String m_phobias = "";
    String m_appearance = "";
    String m_narrative = "";

    public ExtractedEntityData(String aName, int aAge, int aGenderIndex, int aRaceIndex, int aClassIndex, String aLikes, String aDislikes, String aPhobias, String aAppearance, String aNarrative)
    {
        m_name = aName;
        m_age = aAge;
        m_gender = Defines.Gender.values()[aGenderIndex];
        m_race = Defines.Race.values()[aRaceIndex];
        m_class = Defines.Class.values()[aClassIndex];
        m_loves = aLikes;
        m_hates = aDislikes;
        m_phobias = aPhobias;
        m_appearance = aAppearance;
        m_narrative = aNarrative;
    }
    public ExtractedEntityData(String aName, int aAge, String aGender, String aRace, String aClass, String aLikes, String aDislikes, String aPhobias, String aAppearance, String aNarrative)
    {
        m_name = aName;
        m_age = aAge;
        m_genderStr = aGender;
        m_raceStr = aRace;
        m_classStr = aClass;
        m_loves = aLikes;
        m_hates = aDislikes;
        m_phobias = aPhobias;
        m_appearance = aAppearance;
        m_narrative = aNarrative;
    }
}
