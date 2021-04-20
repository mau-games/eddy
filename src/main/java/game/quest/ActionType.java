package game.quest;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public enum ActionType {
    CAPTURE(0),
    DAMAGE(1),
    DEFEND(2),
    ESCORT(3),
    EXCHANGE(4),
    EXPERIMENT(5),
    EXPLORE(6),
    GATHER(7),
    GIVE(8),
    GO_TO(9),
    KILL(10),
    LISTEN(11),
    READ(12),
    REPAIR(13),
    REPORT(14),
    SPY(15),
    STEALTH(16),
    TAKE(17),
    USE(18),
    STEAL(19),
    NONE(19);

    private final int value;

    private ActionType(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ActionType toActionType(int value){
        ActionType type;
        switch (value){
            case 0:
                type = CAPTURE;
                break;
            case 1:
                type = DAMAGE;
                break;
            case 2:
                type = DEFEND;
                break;
            case 3:
                type = ESCORT;
                break;
            case 4:
                type = EXCHANGE;
                break;
            case 5:
                type = EXPERIMENT;
                break;
            case 6:
                type = EXPLORE;
                break;
            case 7:
                type = GATHER;
                break;
            case 8:
                type = GIVE;
                break;
            case 9:
                type = GO_TO;
                break;
            case 10:
                type = KILL;
                break;
            case 11:
                type = LISTEN;
                break;
            case 12:
                type = READ;
                break;
            case 13:
                type = REPAIR;
                break;
            case 14:
                type = REPORT;
                break;
            case 15:
                type = SPY;
                break;
            case 16:
                type = STEALTH;
                break;
            case 17:
                type = TAKE;
                break;
            case 18:
                type = USE;
                break;
            case 19:
            	type = STEAL;
            default:
                type = NONE;
        }
        return type;
    }

    public boolean isExchange() {
        return value == EXCHANGE.getValue();
    }

    public boolean isGive() {
        return value == GIVE.getValue();
    }

    public boolean isTake() {
        return value == TAKE.getValue();
    }

    public boolean isNone() {
        return value == NONE.getValue();
    }
}
