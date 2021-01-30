package game.narrative;

public enum TVTropeType {
    ANY(0),
    HERO(1),
    CONFLICT(2),
    ENEMY(3),
    MODIFIER(4),
    FIVE_MA(11),
    NEO(12),
    SH(13),
    COVS(21),
    COSE(22),
    CONA(23),
    COSO(24),
    EMP(31),
    BAD(32),
    DRA(33),
    CHK(41),
    MCG(42),
    MHQ(43);

    private final int value;

    private TVTropeType(int value){
        this.value = value;
    }



}
