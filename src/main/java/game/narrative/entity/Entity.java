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
    //Narrative
    String m_backstory = "";

    //attributes
    String m_name = "";
    int m_age = 0;
    Defines.Gender m_gender;
    Defines.Race m_race;
    Defines.Class m_class;
    List<Defines.Relationship> relationshipList = new ArrayList<Defines.Relationship>();

    //Attributes setters
    public void SetName(String n){
        m_name = n;
    }
    public void SetAge(int a) { m_age = a;}
    public void SetGender(Defines.Gender g) { m_gender = g;}
    public void SetRace(Defines.Race r) { m_race = r;}
    public void SetClass(Defines.Class c) { m_class = c;}
    public void AddRelation(Defines.Relationship relation){
        relationshipList.add(relation);
    }

    public void RemoveRelation(int toRemove){
        relationshipList.remove(toRemove);
    }

    //attributes, Getters
    public String GetName() { return m_name;};
    public String GetAge(){ return String.valueOf(m_age);}
    public Defines.Gender GetGender() { return m_gender;}
    public Defines.Race GetRace() { return m_race;}
    public Defines.Class GetClass() { return m_class;}
    public List<Defines.Relationship> GetRelations(){ return relationshipList;}

}

