package game.narrative.entity;

public class XML_Entity_Save_Object {

    private String _name;
    private String _age;
/*    private String _gender;
    private String _race;
    private String _class;
    private String _likes;
    private String _dislikes;
    private String _appearance;
    private String _relationship;*/

    public XML_Entity_Save_Object(){

    }

    public XML_Entity_Save_Object(Entity entity){
        this._name = entity.GetName();
        this._age = entity.GetAge();
        //_gender = entity.GetGender().toString();
        //_race = entity.GetRace().toString();
        //_class = entity.GetClass().toString();


        //for(Defines.Relationship r : entity.GetRelations())
          //   _relationship = r.GetRelation().toString();

    }

    public String getName(){return _name;}
    public String getAge(){return _age;}
/*    public String GetGender(){return _gender;}
    public String GetRace(){ return _race;}
    public String GetClass(){return _class;}
    public String GetRelationship(){ return _relationship;}*/

    public void setName(String n){
        _name = n;
    }
    public void setAge(String a) { _age = a;}
/*    public void SetGender(String g) { _gender = g;}
    public void SetRace(String r) { _race = r;}
    public void SetClass(String c) { _class = c;}
    public void SetRelation(String relation){
        _relationship = relation;
    }*/

}
