package game.narrative.entity;

import game.narrative.Defines;
import util.Point;

import java.util.ArrayList;
import java.util.List;

public abstract class Entity {
    public abstract String getURL(); // image location
    public abstract Entity GetEntityType();
    public abstract Point GetPoint();
    public abstract void SetID();

    String m_Entity_ID; // entity ID
    Point m_point;  // entity position

    public String GetID(){ return m_Entity_ID;}
    public String GetNameOrID(){ if(m_name != "") return m_name; else return m_Entity_ID;}

    //attributes
    String m_name = "";
    int m_age = 0;
    String m_genderStr = "";
    String m_raceStr = "";
    String m_classStr = "";
    String m_loves = "";
    String m_hates = "";
    String m_phobias = "";
    String m_appearance = "";
    String m_narrative = "";

    //ENUMS
    Defines.Gender m_genderENUM;
    Defines.Race m_raceENUM;
    Defines.Class m_classENUM;
    List<Defines.Relationship> relationshipList = new ArrayList<Defines.Relationship>();

    //Attributes setters
    public void SetName(String n){
        m_name = n;
    }
    public void SetAge(int a) { m_age = a;}
    public void SetLikes(String likes) { m_loves += " " + likes; }
    public void SetDislikes(String dislikes) { m_hates = " " + dislikes; }
    public void SetAppearance(String app) { m_appearance = app; }
    public void SetGender(Defines.Gender g) { m_genderENUM = g; m_genderStr = m_genderENUM.toString();}
    public void SetRace(Defines.Race r) { m_raceENUM = r; m_raceStr = m_raceENUM.toString();}
    public void SetClass(Defines.Class c) { m_classENUM = c; m_classStr = m_classENUM.toString();}

    public void AddRelation(Defines.Relationship relation){
        relationshipList.add(relation);
    }


    public void RemoveRelation(int toRemove){
        relationshipList.remove(toRemove);
    }

    //attributes, Getters
    public String GetName() { return m_name;};
    public String GetAge(){ return String.valueOf(m_age);}
    public String GetLikes(){return m_loves;}
    public String GetDislikes(){return m_hates;}
    public String GetAppearance(){return m_appearance;}
    public String GetRaceStr(){ return m_raceStr;}
    public String GetGenderStr(){ return m_genderStr;}
    public String GetClassStr(){ return m_classStr;}
    public String GetNarrative(){ return m_narrative;}

    public Defines.Gender GetGender() { return m_genderENUM;}
    public Defines.Race GetRace() { return m_raceENUM;}
    public Defines.Class GetClass() { return m_classENUM;}
    public List<Defines.Relationship> GetRelations(){ return relationshipList;}

    public void ResetEntity(){
        m_name = "";
        m_age = 0;
         m_genderStr = "";
         m_raceStr = "";
         m_classStr = "";
         m_loves = "";
         m_hates = "";
         m_phobias = "";
         m_appearance = "";
         m_narrative = "";

        m_classENUM = null;
        m_genderENUM = null;
        m_raceENUM = null;
        relationshipList.clear();
    }
}

