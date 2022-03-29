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
        List<Entity> m_relationshipTarget;
        AttributeType m_phobiaTarget;
    };
}
