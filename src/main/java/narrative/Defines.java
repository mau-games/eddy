package narrative;

import java.util.List;
import narrative.entity.Entity;

public class Defines {
    public enum RelationshipType{
        Locked,
        Hate,
        Love,
        Family,
        Phobia
    }

    public enum Gender{
        Locked,
        Female,
        Male,
        Other
    }

    public enum Race{
        Locked,
        Orc,
        Human,
        Elf,
        Dwarf
    }

    enum Class{
        Locked,
        Mage,
        Soldier,
        Civilian,
        BountyHunter
    }

    public enum AttributeType{
        Race,
        Element,
        NpcClass,
        Gender,
        Name,
        NameSecond,
        HomeTown,
        EyeColour,
        SkinColour,
        HairColour
    }

    public class Relationship{
        RelationshipType m_relationshipType;
        List<Entity> m_relationshipTarget;
        AttributeType m_phobiaTarget;
    };
}
