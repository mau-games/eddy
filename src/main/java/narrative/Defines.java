package narrative;

import java.util.List;
import narrative.entity.Entity;

public class Defines {
    enum RelationshipType{
        Locked,
        Hate,
        Love,
        Family,
        Phobia
    }

    enum Gender{
        Locked,
        Female,
        Male,
        Other
    }

    enum Race{
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

    enum AttributeType{
        Race,
        Element,
        Class,
        Gender,
        Name,
        NameSecond,
        HomeTown,
        EyeColour,
        SkinColour,
        HairColour
    }

    class Relationship{
        RelationshipType m_relationshipType;
        List<Entity> m_relationshipTarget;
        AttributeType m_phobiaTarget;
    };
}
