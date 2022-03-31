package game.narrative;

import game.narrative.entity.Entity;

import java.util.List;

public class Defines {
    public enum AttributeTypes{
        Name,
        Age,
        Gender,
        Race,
        Class,
        Relationship
    }

    public enum RelationshipType{
        LOCKED,
        Hate,
        Love,
        Family,
        Phobia
    }

    public enum ItemType{
        LOCKED,
        Weapon,
        Armour,
        Potion,
        Accessory
    }

    public enum Element{
        LOCKED,
        Fire,
        Water,
        Earth,
        Air,
        None
    }

    public enum Gender{
        LOCKED,
        Female,
        Male,
        Other
    }

    public enum Race{
        LOCKED,
        Orc,
        Human,
        Elf,
        Dwarf
    }

    public enum Class{
        LOCKED,
        Mage,
        Soldier,
        Civilian,
        BountyHunter
    }

    public enum AttributeType{
        Name,
        Age,
        Gender,
        Race,
        NpcClass,
        Relationship,

        Element,
        NameSecond,
        HomeTown,
        EyeColour,
        SkinColour,
        HairColour,
    }

    public class Relationship{
        RelationshipType m_relationshipType;
        Entity m_relationshipTarget;
        Element m_phobiaTarget;

        public Relationship(RelationshipType relType, Entity target ){
            this.m_relationshipType = relType;
            this.m_relationshipTarget = target;
        }

        public Relationship(Element element ){
            this.m_relationshipType = RelationshipType.Phobia;
            this.m_phobiaTarget = element;
        }

        public String GetName(){ return m_relationshipTarget.GetNameOrID();}
        public RelationshipType GetRelation(){ return m_relationshipType;}
    };
}
