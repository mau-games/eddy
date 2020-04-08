package game.quest;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public enum ActionType {
    capture(0),
    damage(1),
    defend(2),
    escort(3),
    exchange(4),
    experiment(5),
    explore(6),
    gather(7),
    give(8),
    go_to(9),
    kill(10),
    listen(11),
    read(12),
    repair(13),
    report(14),
    spy(15),
    stealth(16),
    take(17),
    use(18),
    none(19);

    private final int value;

    private ActionType(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ActionType ToActionType(int value){
        ActionType type;
        switch (value){
            case 0:
                type = capture;
                break;
            case 1:
                type = damage;
                break;
            case 2:
                type = defend;
                break;
            case 3:
                type = escort;
                break;
            case 4:
                type = exchange;
                break;
            case 5:
                type = experiment;
                break;
            case 6:
                type = explore;
                break;
            case 7:
                type = gather;
                break;
            case 8:
                type = give;
                break;
            case 9:
                type = go_to;
                break;
            case 10:
                type = kill;
                break;
            case 11:
                type = listen;
                break;
            case 12:
                type = read;
                break;
            case 13:
                type = repair;
                break;
            case 14:
                type = report;
                break;
            case 15:
                type = spy;
                break;
            case 16:
                type = stealth;
                break;
            case 17:
                type = take;
                break;
            case 18:
                type = use;
                break;
            default:
                type = none;
        }
        return type;
    }
}
