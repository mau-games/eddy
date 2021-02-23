package gui.controls;

import collectors.ActionLogger;
import game.MapContainer;
import game.WorldViewCanvas;
import game.narrative.TVTropeType;
import gui.utils.DungeonDrawer;
import gui.utils.MoveElementBrush;
import gui.utils.RoomConnectorBrush;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import util.Point;
import util.Util;
import util.eventrouting.EventRouter;
import util.eventrouting.events.FocusRoom;
import util.eventrouting.events.RequestRoomView;

import java.util.ArrayList;

public class NarrativeShape extends StackPane
{
    Shape shape;
    Label other_text;
    TVTropeType trope_type;

    private double max_size = 100.0;
    private double half_size = max_size/2.0;
    private double quarter_size = max_size/4.0;
    private ArrayList<Point> border_positions = new ArrayList<Point>();

    private NarrativeShape self;
    private Node source;

    //We should add a few values to make our life easier
    private boolean rendered;
    public float viewSizeHeight;
    public float viewSizeWidth;
    private double dragAnchorX;
    private double dragAnchorY;
    private double prevPositionX;
    private double prevPositionY;

    public DoubleProperty xPosition; //TODO: public just to test
    public DoubleProperty yPosition;

    public DoubleProperty tileSizeWidth; //TODO: public just to test
    public DoubleProperty tileSizeHeight;

    private Point currentBrushPosition = new Point();

    public NarrativeShape(TVTropeType trope_type)
    {
        self = this;
        this.trope_type = trope_type;
        recreateShape(trope_type);
        other_text.setStyle("-fx-text-fill: red;");
        other_text.setFont(new Font("Arial", 21));

        this.getChildren().add(shape);
//        this.getChildren().add(narrative_text);
        this.getChildren().add(other_text);
        this.setBorder(new Border(new BorderStroke(Color.GREEN,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        //FOR INTERACTION

        this.addEventFilter(MouseEvent.MOUSE_ENTERED, new MouseEventH());
        this.addEventFilter(MouseEvent.MOUSE_DRAGGED, new MouseEventDrag());
        this.setOnDragDetected(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event) //TODO: THERE ARE SOME ERRORS FROM THIS POINT!!
            {
                self.startFullDrag();

            }
        });

        rendered = false;
        viewSizeHeight = 0;
        viewSizeWidth = 0;
        tileSizeHeight = new SimpleDoubleProperty();
        tileSizeWidth = new SimpleDoubleProperty();
        xPosition = new SimpleDoubleProperty();
        yPosition = new SimpleDoubleProperty();

        //Crazy binding but it gives exactly the position we need and now it is updated  automatically
        xPosition.bind(Bindings.selectDouble(this.boundsInParentProperty(), "minX").
                add(Bindings.divide( Bindings.selectDouble(this.boundsInParentProperty(), "width"), 2).subtract(Bindings.divide(this.widthProperty(), 2.0))));
        yPosition.bind(Bindings.selectDouble(this.boundsInParentProperty(), "minY").
                add(Bindings.divide( Bindings.selectDouble(this.boundsInParentProperty(), "height"), 2).subtract(Bindings.divide(this.heightProperty(), 2.0))));

    }

    public void recreateShape(TVTropeType trope_type)
    {
        border_positions = new ArrayList<Point>();
        shape = new Polygon();
        other_text = new Label(trope_type.name());

        if(trope_type.getValue() == 0)
        {
            //ANY
        }
        else if (trope_type.getValue() >= 40)
        {
            //MODIFIERS - CIRCLE
            shape = new Circle();
            ((Circle)shape).setCenterX(half_size);
            ((Circle)shape).setCenterY(half_size);
            ((Circle)shape).setRadius(half_size);

            border_positions.add(new Point(half_size, 0));      //N
            border_positions.add(new Point(max_size, half_size));  //E
            border_positions.add(new Point(half_size, max_size));  //S
            border_positions.add(new Point(0, half_size));      //W

        }
        else if (trope_type.getValue() >= 30)
        {
            //ENEMIES - HEXAGON
            ((Polygon)shape).getPoints().addAll(0.0, half_size,
                    quarter_size, max_size,
                    max_size - quarter_size, max_size,
                    max_size, half_size,
                    max_size - quarter_size, 0.0,
                    quarter_size, 0.0);

            border_positions.add(new Point(half_size, 0));      //N
            border_positions.add(new Point(max_size, half_size));  //E
            border_positions.add(new Point(half_size, max_size));  //S
            border_positions.add(new Point(0, half_size));      //W
        }
        else if (trope_type.getValue() >= 20)
        {
            //Conflicts - ROMBO
//            shape.getPoints().addAll(300.0 * 0.3, 50.0 * 0.3,
//                    450.0 * 0.3, 150.0 * 0.3,
//                    300.0 * 0.3, 250.0 * 0.3,
//                    150.0 * 0.3, 150.0 * 0.3);

            ((Polygon)shape).getPoints().addAll(0.0, half_size,
                    half_size, max_size,
                    max_size, half_size,
                    half_size, 0.0);

            border_positions.add(new Point(half_size, 0));      //N
            border_positions.add(new Point(max_size, half_size));  //E
            border_positions.add(new Point(half_size, max_size));  //S
            border_positions.add(new Point(0, half_size));      //W
        }
        else if (trope_type.getValue() >= 10)
        {
            //HEROES - RECTANGLE
            ((Polygon)shape).getPoints().addAll(0.0, 0.0,
                    0.0, half_size,
                    max_size, half_size,
                    max_size, 0.0);

            border_positions.add(new Point(half_size, 0));      //N
            border_positions.add(new Point(max_size, quarter_size));  //E
            border_positions.add(new Point(half_size, half_size));  //S
            border_positions.add(new Point(0, quarter_size));      //W

            //Heroes
            if(trope_type == TVTropeType.FIVE_MA)
                other_text = new Label("5MA");
        }
    }

    public void setRendered(boolean value)
    {
        rendered = value;
    }

    public boolean getRendered() { return rendered; }


    public Point getRNDPositionShapeBorder()
    {
        return border_positions.get(Util.getNextInt(0, border_positions.size()));
    }

    public Point getPositionShapeBorder(int index)
    {
        return border_positions.get(index);
    }

    public class MouseEventDrag implements EventHandler<MouseEvent>
    {
        @Override
        public void handle(MouseEvent event)
        {
            source = (Node)event.getSource();

            source.setOnMouseDragEntered(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event)
                {
//                    if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnectorBrush)
//                    {
//                        borderCanvas.setVisible(true);
//                        drawBorder();
//                    }
//                    else
//                    {
//                        highlight(true);
//                    }
//
//                    DungeonDrawer.getInstance().getBrush().onEnteredRoom(owner);
                }
            });

            source.setOnMouseDragged(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event)
                {

                    source.setTranslateX(event.getX() + source.getTranslateX() - dragAnchorX);
                    source.setTranslateY(event.getY() + source.getTranslateY() - dragAnchorY);

//                    if(DungeonDrawer.getInstance().getBrush() instanceof MoveElementBrush)
//                    {
//                        source.setTranslateX(event.getX() + source.getTranslateX() - dragAnchorX);
//                        source.setTranslateY(event.getY() + source.getTranslateY() - dragAnchorY);
//                    }
//                    else if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnectorBrush)
//                    {
//                        currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));
//                        drawBorder();
//                    }
                }

            });

            source.setOnMouseDragOver(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event)
                {
//                    if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnectorBrush)
//                    {
//                        currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));
//                        drawBorder();
//                    }

                }

            });

            source.setOnMouseDragReleased(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event)
                {
                    currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));
//
//                    if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnectorBrush)
//                    {
//                        if(owner.isPointInBorder(currentBrushPosition))
//                            DungeonDrawer.getInstance().getBrush().onReleaseRoom(owner, currentBrushPosition);
//                    }
//                    else
//                    {
//                        DungeonDrawer.getInstance().getBrush().onReleaseRoom(owner, currentBrushPosition);
//                    }

                    Bounds ltoS = self.localToScene(self.getBoundsInLocal());
                    double newPositionX = ltoS.getMaxX() - (ltoS.getWidth() / 2.0);
                    double newPositionY = ltoS.getMaxY() - (ltoS.getHeight() / 2.0);

//                    ActionLogger.getInstance().storeAction(ActionLogger.ActionType.CHANGE_POSITION,
//                            ActionLogger.View.WORLD,
//                            ActionLogger.TargetPane.WORLD_MAP_PANE,
//                            false,
//                            owner,
//                            prevPositionX, //Point A
//                            prevPositionY, //Point A
//                            newPositionX, //Point B
//                            newPositionY //point B
//                    );
                }

            });

            source.setOnMouseDragExited(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event)
                {
//                    borderCanvas.setVisible(false);
                }

            });

        }
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
//                    if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnectorBrush)
//                    {
//                        borderCanvas.setVisible(true);
//                        drawBorder();
//                    }
//                    else
//                    {
//                        highlight(true);
//                    }
//
//                    DungeonDrawer.getInstance().getBrush().onEnteredRoom(owner); //This could also use the event actually :O
                }

            });

            //2) mouse is moved around the map
            source.setOnMouseMoved(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event)
                {
                    currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));
//                    drawBorder();
                }
            });

            source.setOnMousePressed(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event)
                {
//                    if(event.getClickCount() == 2) //Works --> Double click
//                    {
//                        //TODO: I think is better that the dungeon receives an event to request room view but maybe that will be too convoluted
//                        MapContainer mc = new MapContainer(); // this map container thingy, idk, me not like it
//                        mc.setMap(owner);
//                        EventRouter.getInstance().postEvent(new RequestRoomView(mc, 0, 0, null));
//                    }

//                    EventRouter.getInstance().postEvent(new FocusRoom(owner, null));

                    dragAnchorX = event.getX();
                    dragAnchorY = event.getY();
                    Bounds ltoS = self.localToScene(self.getBoundsInLocal());
                    prevPositionX = ltoS.getMaxX() - (ltoS.getWidth() / 2.0);
                    prevPositionY = ltoS.getMaxY() - (ltoS.getHeight() / 2.0);

                    currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));
//
//                    if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnectorBrush)
//                    {
//                        if(owner.isPointInBorder(currentBrushPosition))
//                            DungeonDrawer.getInstance().getBrush().onClickRoom(owner,currentBrushPosition);
//                    }
//                    else
//                    {
//                        DungeonDrawer.getInstance().getBrush().onClickRoom(owner,currentBrushPosition);
//                    }

                }
            });

            source.setOnMouseReleased(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event)
                {
                    currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));
                }
            });

            source.setOnMouseExited(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event)
                {
//                    borderCanvas.setVisible(false);
                }

            });
        }
    }
}
