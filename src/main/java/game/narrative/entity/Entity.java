package game.narrative.entity;

import game.narrative.Defines;
import util.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public String GetRelationStringAt(int at) {
        if(relationshipList.get(at) == null)
            return null;
        return relationshipList.get(at).GetRelationString();
    }

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

    public void Copy(Entity other){
        this. m_name = other.m_name;
        this. m_age = other.m_age;
        this. m_genderStr = other.m_genderStr;
        this. m_raceStr = other.m_raceStr;
        this. m_classStr = other.m_classStr;
        this. m_loves = other.m_loves;
        this. m_hates = other.m_hates;
        this. m_phobias = other.m_phobias;
        this. m_appearance = other.m_appearance;
        this. m_narrative = other.m_narrative;

        //ENUMS
        this.m_genderENUM = other.m_genderENUM;
        this.m_raceENUM = other.m_raceENUM;
        this.m_classENUM = other.m_classENUM;
        this.relationshipList = other.relationshipList;
    }
    public String ToModellString(String aName, String aAge, String aGender, String aRace, String aAppearance, String aLoves, String aHates, String aNarrative){
        String modellStr = "<entry>";
        if(!Objects.equals(m_name, ""))
        {
            modellStr += "<name>" + m_name +"</name>";
        }
        else
        {
            modellStr += "<name>" + aName +"</name>";
        }
        if(!Objects.equals(String.valueOf(m_age), "0"))
        {
            modellStr += "<age>" + m_age +"</age>";
        }
        else
        {
            modellStr += "<age>" + aAge +"</age>";
        }
        if(!Objects.equals(m_genderStr, ""))
        {
            modellStr += "<gender>" + m_genderStr +"</gender>";
        }
        else
        {
            modellStr += "<gender>" + aGender +"</gender>";
        }
        if(!Objects.equals(m_raceStr, ""))
        {
            modellStr += "<race>" + m_raceStr +"</race>";
        }
        else
        {
            modellStr += "<race>" + aRace +"</race>";
        }
        if(!Objects.equals(m_appearance, ""))
        {
            modellStr += "<appearance>" + m_appearance +"</appearance>";
        }
        else
        {
            modellStr += "<appearance>" + aAppearance +"</appearance>";
        }
        if(!Objects.equals(m_loves, ""))
        {
            modellStr += "<loves>" + m_loves +"</loves>";
        }
        else
        {
            modellStr += "<loves>" + aLoves +"</loves>";
        }
        if(!Objects.equals(m_hates, ""))
        {
            modellStr += "<hates>" + m_loves +"</hates>";
        }
        else
        {
            modellStr += "<hates>" + aHates +"</hates>";
        }
        if(!Objects.equals(m_narrative, ""))
        {
            modellStr += "<narrative>" + m_narrative +"</narrative>";
        }
        else
        {
            modellStr += "<narrative>" + aNarrative +"</narrative>";
        }
        modellStr += "</entry>";

        return modellStr;
    }
}

