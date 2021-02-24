package gui.controls;

import game.narrative.TVTropeType;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import util.eventrouting.EventRouter;
import util.eventrouting.events.RequestNewGrammarStructureNode;

public class TVTropeMenuItem extends MenuItem
{
    String name;
    TVTropeType trope;

    public TVTropeMenuItem(String name, TVTropeType trope)
    {
        super(name);
        this.trope = trope;

        this.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

//                System.out.println(((TVTropeMenuItem)event.getSource()).getParentPopup().getX());
//                System.out.println(((TVTropeMenuItem)event.getSource()).getParentPopup().getAnchorX());
                
//                System.out.println(((TVTropeMenuItem)event.getSource()).getParentPopup().get());
                EventRouter.getInstance().postEvent(new RequestNewGrammarStructureNode(
                        trope,
                        ((TVTropeMenuItem)event.getSource()).getParentPopup().getAnchorX(),
                        ((TVTropeMenuItem)event.getSource()).getParentPopup().getAnchorY()));
//                    System.out.println("Cut..." + trope.name());
            }
        });
    }

}
