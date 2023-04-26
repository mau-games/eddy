package gui.controls;

import game.Room;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import util.eventrouting.EventRouter;
import util.eventrouting.events.ScaledMapSelected;
import util.eventrouting.events.SuggestedMapSelected;

public class ScaledRoom {
    private LabeledCanvas roomViewNode;
    private Node source;
    private Room scaledRoom;
    private Room eaScaledRoom;
    private boolean selected = false;
    private ScaledRoom self;
    public ScaledRoom()
    {
        roomViewNode = new LabeledCanvas();
        roomViewNode.setPrefSize(140, 140);
        roomViewNode.addEventFilter(MouseEvent.MOUSE_ENTERED, new MouseEventH());
        self = this;
        selected = false;
    }

    public class MouseEventH implements EventHandler<MouseEvent>
    {
        @Override
        public void handle(MouseEvent event)
        {
            source = (Node)event.getSource();

            //1) Mouse enters the canvas of the room --> I can fire the event here, no?
            source.setOnMouseEntered(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event)
                {
                    highlight(true);
                    System.out.println("MOUSE ENTERED CANVAS");
                }

            });

            //2) mouse is moved around the map
            source.setOnMouseMoved(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event)
                {
                }
            });

            source.setOnMousePressed(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event)
                {
                    EventRouter.getInstance().postEvent(new ScaledMapSelected(self));
                    selected = true;
                    highlight(true);

                }
            });

            source.setOnMouseReleased(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event)
                {
                }
            });

            source.setOnMouseExited(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event)
                {
                    highlight(false);
                }

            });
        }
    }
    public LabeledCanvas getRoomCanvas()
    {
        return roomViewNode;
    }

    public Room getScaledRoom(){
        return scaledRoom;
    }

    public void setScaledRoom(Room scaledRoom) {
        this.scaledRoom = scaledRoom;
    }

    public void setSelected(Boolean value)
    {
        selected = value;
        highlight(value);
    }

    private void highlight(boolean state)
    {
        if(selected)
        {
            roomViewNode.setStyle("-fx-border-width: 2px; -fx-border-color: #fcdf3c;");
        }
        else
        {
            if (state) {
                roomViewNode.setStyle("-fx-border-width: 2px; -fx-border-color: #6b87f9;");
            } else {
                roomViewNode.setStyle("-fx-border-width: 0px; -fx-background-color:#2c2f33;");
            }
        }
    }
}
