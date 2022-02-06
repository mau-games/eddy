package designerModeling;

import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.RoomEdited;
import util.eventrouting.events.despers.DesPersEvaluation;

/**
 * Probably this will change to this not being the static class (singleton).
 * Rather there will be a collection to be accessible, in case we want to also account for other models rather than
 * the current designer.
 *
 * -- Side project from that, could be how to encounter differences between designer models, i.e, when a model drifts
 *  away from another.
 */
public class DesignerModel implements Listener {

    private static DesignerModel instance = null;

    /** The first of many other models? missing:
     *
     * - Preference model (now might be better since I could use pytorch?)
     * - Goal model? (a more explicit goal model)
     * - Aesthetic model?
     *
     */
    public DesignerPersona designer_persona;

    private static EventRouter router = EventRouter.getInstance();

    private DesignerModel()
    {
//        router.registerListener(this, new AlgorithmDone(null, null, null));
        router.registerListener(this, new RoomEdited(null));
        router.registerListener(this, new DesPersEvaluation(null));

        designer_persona = new DesignerPersona();

    }

    /**
     * Gets the singleton instance of this class.
     *
     * @return An instance of MapRenderer.
     */
    public static DesignerModel getInstance() {
        if (instance == null) {
            instance = new DesignerModel();
        }
        return instance;
    }

    @Override
    public void ping(PCGEvent e) {

    }
}
