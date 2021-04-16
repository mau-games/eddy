package game.narrative;

public enum TVTropeType {
    ANY(0),
    HERO(10),
    CONFLICT(20),
    ENEMY(30),
    PLOT_DEVICE(40),
    FIVE_MA(11),
    NEO(12),
    SH(13), //Superhero
//    COVS(21), //Conflict against another
//    COSE(22), //Conflict against self?
    CONA(23), //Conflict against nature?
    COSO(24), //Conflict against society
    EMP(31), //The empire
    BAD(32), //BAD
    DRA(33), //Drake
    CHK(41), //Chekov's gun
    MCG(42), //Macguffin
    MHQ(43); //May help you on your quest

    private final int value;

    private TVTropeType(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public TVTropeType getGeneric()
    {
        if(this.getValue() == 0)
            return TVTropeType.ANY;
        else if (this.getValue() >= 40)
            return TVTropeType.PLOT_DEVICE;
        else if (this.getValue() >= 30)
            return TVTropeType.ENEMY;
        else if (this.getValue() >= 20)
            return TVTropeType.CONFLICT;
        else if (this.getValue() >= 10)
            return TVTropeType.HERO;

        return TVTropeType.ANY;
    }

}
