package gui.controls;

import game.CoCreativity.AICoCreator;
import game.CoCreativity.HumanCoCreator;
import game.Room;
import game.Tile;
import game.TileTypes;
import gui.utils.InformativePopupManager;
import gui.utils.MapRenderer;
import gui.views.CCRoomViewController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.intraview.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditedRoomStackPane extends StackPane implements Listener
{
    public UUID uniqueID;

    public InteractiveMap editedPane;
    public Canvas aiPlacedTintCanvas;
    public Canvas aiSuggestionCanvas;
    public Canvas aiSuggestionTintCanvas;
    public Canvas patternCanvas;
    public Canvas warningCanvas;
    public Canvas lockCanvas;
    public Canvas brushCanvas;
    public Canvas tileCanvas;

    int mapWidth;
    int mapHeight;

    public Drawer currentBrush;
    private MapRenderer renderer = MapRenderer.getInstance();

    private boolean initialized = false;


    /**
     * Creates an empty instance of InteractiveMap.
     */
    public EditedRoomStackPane() {
        super();
        uniqueID = UUID.randomUUID();
//        EventRouter.getInstance().registerListener(new Edite);
        EventRouter.getInstance().registerListener(this, new InteractiveRoomBrushUpdated(null, null));
        EventRouter.getInstance().registerListener(this, new EditedRoomToggleLocks(false));
        EventRouter.getInstance().registerListener(this, new EditedRoomTogglePatterns(false));
//        EventRouter.getInstance().registerListener(this, new SaveDisplayedCells());
    }

    public void init(int mapWidth, int mapHeight, Room room)
    {
        initialized = true;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;

        this.getChildren().clear();
        editedPane = new InteractiveMap();
        editedPane.init(this, uniqueID);

        StackPane.setAlignment(editedPane, Pos.CENTER);
        editedPane.setMinSize(mapWidth, mapHeight);
        editedPane.setMaxSize(mapWidth, mapHeight);
        editedPane.setPrefSize(mapWidth, mapHeight);
        this.getChildren().add(editedPane);
        editedPane.updateMap(room);


        aiPlacedTintCanvas = new Canvas(mapWidth, mapHeight);
        StackPane.setAlignment(aiPlacedTintCanvas, Pos.CENTER);
        this.getChildren().add(aiPlacedTintCanvas);
        aiPlacedTintCanvas.setVisible(false);
        aiPlacedTintCanvas.setMouseTransparent(true);
        aiPlacedTintCanvas.setOpacity(0.3f);

        aiSuggestionCanvas = new Canvas(mapWidth, mapHeight);
        StackPane.setAlignment(aiSuggestionCanvas, Pos.CENTER);
        this.getChildren().add(aiSuggestionCanvas);
        aiSuggestionCanvas.setVisible(false);
        aiSuggestionCanvas.setMouseTransparent(true);
        aiSuggestionCanvas.setOpacity(1.0);

        aiSuggestionTintCanvas = new Canvas(mapWidth, mapHeight);
        StackPane.setAlignment(aiSuggestionTintCanvas, Pos.CENTER);
        this.getChildren().add(aiSuggestionTintCanvas);
        aiSuggestionTintCanvas.setVisible(false);
        aiSuggestionTintCanvas.setMouseTransparent(true);
        aiSuggestionTintCanvas.setOpacity(0.3f);

        brushCanvas = new Canvas(mapWidth, mapHeight);
        StackPane.setAlignment(brushCanvas, Pos.CENTER);
        this.getChildren().add(brushCanvas);
        brushCanvas.setVisible(false);
        brushCanvas.setMouseTransparent(true);
        brushCanvas.setOpacity(1.0f);

        lockCanvas = new Canvas(mapWidth, mapHeight);
        StackPane.setAlignment(lockCanvas, Pos.CENTER);
        this.getChildren().add(lockCanvas);
        lockCanvas.setVisible(false);
        lockCanvas.setMouseTransparent(true);
        lockCanvas.setOpacity(0.4f);

        patternCanvas = new Canvas(mapWidth, mapHeight);
        StackPane.setAlignment(patternCanvas, Pos.CENTER);
        this.getChildren().add(patternCanvas);
        patternCanvas.setVisible(false);
        patternCanvas.setMouseTransparent(true);

		tileCanvas = new Canvas(mapWidth, mapHeight);
		StackPane.setAlignment(tileCanvas, Pos.CENTER);
		this.getChildren().add(tileCanvas);
		tileCanvas.setVisible(true);
		tileCanvas.setMouseTransparent(true);

        warningCanvas = new Canvas(mapWidth, mapHeight);
        StackPane.setAlignment(warningCanvas, Pos.CENTER);
        this.getChildren().add(warningCanvas);
        warningCanvas.setVisible(false);
        warningCanvas.setMouseTransparent(true);

        GraphicsContext gc = warningCanvas.getGraphicsContext2D();
		gc.setStroke(Color.rgb(255, 0, 0, 1.0));
		gc.setLineWidth(3);
		gc.strokeRect(1, 1, mapWidth - 1, mapHeight - 1);
		gc.setLineWidth(1);
		gc.setStroke(Color.rgb(255, 0, 0, 0.9));
		gc.strokeRect(3, 3, mapWidth - 6, mapHeight - 6);
		gc.setStroke(Color.rgb(255, 0, 0, 0.8));
		gc.strokeRect(4, 4, mapWidth - 8, mapHeight - 8);
		gc.setStroke(Color.rgb(255, 0, 0, 0.7));
		gc.strokeRect(5, 5, mapWidth - 10, mapHeight - 10);
		gc.setStroke(Color.rgb(255, 0, 0, 0.6));
		gc.strokeRect(6, 6, mapWidth - 12, mapHeight - 12);
		gc.setStroke(Color.rgb(255, 0, 0, 0.5));
		gc.strokeRect(7, 7, mapWidth - 14, mapHeight - 14);
		gc.setStroke(Color.rgb(255, 0, 0, 0.4));
		gc.strokeRect(8, 8, mapWidth - 16, mapHeight - 16);
		gc.setStroke(Color.rgb(255, 0, 0, 0.3));
		gc.strokeRect(9, 9, mapWidth - 18, mapHeight - 18);
		gc.setStroke(Color.rgb(255, 0, 0, 0.2));
		gc.strokeRect(10, 10, mapWidth - 20, mapHeight - 20);
		gc.setStroke(Color.rgb(255, 0, 0, 0.1));
		gc.strokeRect(11, 11, mapWidth - 22, mapHeight - 22);
    }

    public void mapIsFeasible(boolean state) {
        warningCanvas.setVisible(!state);

        if(!state)
            InformativePopupManager.getInstance().requestPopup(editedPane, InformativePopupManager.PresentableInformation.ROOM_INFEASIBLE, "");
    }

    /**
     * Redraws the pattern, based on the current map layout.

     */
    private synchronized void redrawPatterns(Room room) {
        //Change those 2 width and height hardcoded values (420,420)
        //And change zone to its own method
        patternCanvas.getGraphicsContext2D().clearRect(0, 0, mapWidth, mapHeight);

        renderer.drawPatterns(patternCanvas.getGraphicsContext2D(), room.toMatrix(), renderer.colourPatterns(room.getPatternFinder().findMicroPatterns()));
        renderer.drawGraph(patternCanvas.getGraphicsContext2D(), room.toMatrix(), room.getPatternFinder().getPatternGraph());
        renderer.drawMesoPatterns(patternCanvas.getGraphicsContext2D(), room.toMatrix(), room.getPatternFinder().getMesoPatterns());
    }

    /***
     * Redraw the lock in the map --> TODO: I am afraid this should be in the renderer
     * @param room
     */
    private void redrawLocks(Room room)
    {
        lockCanvas.getGraphicsContext2D().clearRect(0, 0, mapWidth, mapHeight);

        for(int i = 0; i < room.getRowCount(); ++i)
        {
            for(int j = 0; j < room.getColCount(); ++j)
            {
                if(room.getTile(j, i).GetImmutable())
                {
                    lockCanvas.getGraphicsContext2D().drawImage(renderer.GetLock(editedPane.scale * 0.75f,
                            editedPane.scale * 0.75f),
                            j * editedPane.scale, i * editedPane.scale);
//					lockCanvas.getGraphicsContext2D().drawImage(renderer.GetLock(mapView.scale * 3.0f, mapView.scale * 3.0f), (j-1) * mapView.scale, (i-1) * mapView.scale);
                }
            }
        }
    }

    private void drawTintAiPlacedTiles(Room room)
    {
        aiPlacedTintCanvas.getGraphicsContext2D().clearRect(0,0, mapWidth, mapHeight);
        aiPlacedTintCanvas.setVisible(true);

        for(int i = 0; i < room.getRowCount(); ++i)
        {
            for(int j = 0; j < room.getColCount(); ++j)
            {
                if(room.getTile(j, i).getPlacedByAI())
                {
                    Drawer brush = new Drawer();
                    brush.SetMainComponent(room.getTile(j, i).GetType());

                    aiPlacedTintCanvas.getGraphicsContext2D().drawImage(renderer.GetTint(editedPane.scale, editedPane.scale),
                            j * editedPane.scale, i * editedPane.scale);

                    //renderer.drawBrush(aiPlacedTintCanvas.getGraphicsContext2D(),
                    //        room.toMatrix(), brush,
                    //        Color.BLUE);
                }
            }
        }
    }

    private void drawSuggestionsTiles(InteractiveMap map, Room room, List<Tile> tiles)
    {
        aiSuggestionCanvas.getGraphicsContext2D().clearRect(0,0, mapWidth, mapHeight);
        aiSuggestionCanvas.setVisible(true);
        aiSuggestionTintCanvas.getGraphicsContext2D().clearRect(0,0, mapWidth, mapHeight);
        aiSuggestionTintCanvas.setVisible(true);

        for(Tile t: tiles)
        {
            //get position
            int x = t.GetCenterPosition().getX();
            int y = t.GetCenterPosition().getY();

            //Draw image for tiletype
            aiSuggestionCanvas.getGraphicsContext2D().drawImage(renderer.renderTile(t.GetType(), editedPane.scale, editedPane.scale),x * editedPane.scale , y * editedPane.scale );

            //draw tint
            aiSuggestionTintCanvas.getGraphicsContext2D().drawImage(renderer.GetSuggestionTint(editedPane.scale, editedPane.scale),x * editedPane.scale, y * editedPane.scale);

        }
    }

    public void clearAISuggestionView()
    {
        aiSuggestionCanvas.setVisible(false);
        aiSuggestionTintCanvas.setVisible(false);
    }

    //TODO: CHECK THIS!
    public boolean checkInfeasibleLockedRoom(ImageView tile, InteractiveMap editedRoomCanvas)
    {
        Room auxRoom = new Room(editedRoomCanvas.getMap());
        editedRoomCanvas.updateTileInARoom(auxRoom, tile, currentBrush);

        if(!auxRoom.walkableSectionsReachable())
        {
            System.out.println("I DETECTED IT!!");
            InformativePopupManager.getInstance().requestPopup(editedRoomCanvas, InformativePopupManager.PresentableInformation.ROOM_INFEASIBLE_LOCK, "");
            return true;
        }

        return false;
    }

    public void RoomHovered(InteractiveMap editedRoomCanvas, Room hoveredRoom, util.Point p, MouseEvent event)
    {
        //System.out.println("ROOM HOVERED");
        brushCanvas.setVisible(false);
        brushCanvas.getGraphicsContext2D().clearRect(0, 0, mapWidth, mapHeight); //Needs to change!
        brushCanvas.setVisible(true);

//        currentBrush.SetBrushSize((int)(brushSlider.getValue()));
        currentBrush.Update(event, p, hoveredRoom);

        renderer.drawBrush(((EditedRoomStackPane)editedRoomCanvas.owner).brushCanvas.getGraphicsContext2D(),
                hoveredRoom.toMatrix(), currentBrush,
                currentBrush.possibleToDraw() ? Color.WHITE : Color.RED);
    }

    public void RoomEdited(InteractiveMap editedRoomCanvas, Room editedRoom, ImageView tile, MouseEvent event)
    {
        util.Point p = editedPane.CheckTile(tile);

        if(editedRoom.getTile(p.getX(), p.getY()).getEditable() && HumanCoCreator.getInstance().getAmountOfTilesPlaced() < HumanCoCreator.getInstance().getMaxTilesPerRound())
        {
            System.out.println("ROOM EDITED");

            currentBrush.UpdateModifiers(event);
//				mapView.updateTile(tile, brush, event.getButton() == MouseButton.SECONDARY, lockBrush.isSelected() || event.isControlDown());

            if(!currentBrush.possibleToDraw() || (currentBrush.GetModifierValue("Lock") && checkInfeasibleLockedRoom(tile, editedRoomCanvas)))
                return;

            if(currentBrush.GetModifierValue("Lock"))
            {
                InformativePopupManager.getInstance().requestPopup(editedRoomCanvas, InformativePopupManager.PresentableInformation.LOCK_RESTART, "");
            }

            editedRoomCanvas.updateTile(tile, currentBrush);

            editedRoom.forceReevaluation();
            editedRoom.getRoomXML("room\\");

            mapIsFeasible(editedPane.getMap().isIntraFeasible());
            redrawPatterns(editedPane.getMap());
            redrawLocks(editedPane.getMap());
            drawTintAiPlacedTiles(editedPane.getMap());

            HumanCoCreator.getInstance().RegisterContributionInfo(editedRoom.getTile(p));


//        EventRouter.getInstance().postEvent(new InteractiveRoomEdited(self, getMap(), tile, event));
            EventRouter.getInstance().postEvent(new UserEditedRoom(uniqueID, editedPane.getMap()));


//        EventRouter.getInstance().postEvent(new EditedRoomRedrawCanvas(editedRoomCanvas.ownerID));
//        EventRouter.getInstance().postEvent(new RoomEdited(editedRoom));

            //register contribution to HumanCoCreator class

        }
        else if(HumanCoCreator.getInstance().getAmountOfTilesPlaced() >= HumanCoCreator.getInstance().getMaxTilesPerRound())
        {
            System.out.println("COULD NOT EDIT - MAX AMOUNT OF THIS ROUND PLACED");
        }
        else
        {
            System.out.println("COULD NOT EDIT AI PLACED TILE");
        }
    }



    /***
     * The Method that AI uses to edit the room
     * @param
     */
    public void CCRoomEdited(Room editedRoom, List<Tile> tiles)
    {
        for(Tile t:tiles)
        {
            if(t != null)
            {
                Drawer tempbrush = (t.GetTypeAsBrush());

                // from Drawer.update()
                ImageView tImgView = (ImageView) editedPane.getCell(t.GetCenterPosition().getX(), t.GetCenterPosition().getY());
                util.Point p = editedPane.CheckTile(tImgView);

                System.out.println("P: " + p);
                tempbrush.brush.UpdateDrawableTiles(p.getX(), p.getY(), editedPane.getMap());

                editedPane.updateTileInARoom(editedPane.getMap(), tImgView, tempbrush);

                editedRoom.getTile(p.getX(), p.getY()).setEditable(true); //
                editedRoom.getTile(p.getX(), p.getY()).setPlacedByAI(true); //

                //necessary checks and procedures
                editedRoom.forceReevaluation();
                editedRoom.getRoomXML("room\\");

                Platform.runLater(() -> {
                    mapIsFeasible(editedPane.getMap().isIntraFeasible());
                    redrawPatterns(editedPane.getMap());
                    redrawLocks(editedPane.getMap());
                    drawTintAiPlacedTiles(editedPane.getMap());
                    AICoCreator.getInstance().getCcRoomViewController().updateRoom(editedPane.getMap());
                });

                System.out.println("ROOM EDITED BY AI " + t.GetType().name() + " " + p.toString());
            }
            //EventRouter.getInstance().postEvent(new UserEditedRoom(uniqueID, editedPane.getMap()));
        }

        Platform.runLater(() -> {
            System.out.println("UPDATE ROOM");
            AICoCreator.getInstance().getCcRoomViewController().updateRoom(editedPane.getMap());
        });
    }

    public void PlaceSuggestion(Room editedRoom, List<Tile> tiles, MouseEvent event)
    {
        // Point of where you clicked
        ImageView imgView = (ImageView) event.getTarget();

        util.Point p = editedPane.CheckTile(imgView);
        int i=0;

        for(; i<tiles.size();i++)
        {
            Point tp = new Point(tiles.get(i).GetCenterPosition().getX(), tiles.get(i).GetCenterPosition().getY());

            if(tp.getX() == p.getX() && tp.getY() == p.getY()) //if tile was clicked
            {
                //create a brush
                Drawer tempbrush = (tiles.get(i).GetTypeAsBrush());
                //update the map with the brsh
                tempbrush.brush.UpdateDrawableTiles(p.getX(), p.getY(), editedPane.getMap());

                editedPane.updateTileInARoom(editedPane.getMap(), imgView, tempbrush);

                editedRoom.getTile(p.getX(), p.getY()).setEditable(true); //
                editedRoom.getTile(p.getX(), p.getY()).setPlacedByAI(true); //

                //necessary checks and procedures
                editedRoom.forceReevaluation();
                editedRoom.getRoomXML("room\\");

                AICoCreator.getInstance().removeTileFromContributions(tiles.get(i));

                System.out.println("suggestions after remove: " +AICoCreator.getInstance().GetContributions().toString());
                System.out.println("SUGGESTION PLACED: " + tiles.get(i).GetType().name() + " " + p.toString());

                break;
            }
        }

        Platform.runLater(() -> {
            mapIsFeasible(editedPane.getMap().isIntraFeasible());
            redrawPatterns(editedPane.getMap());
            redrawLocks(editedPane.getMap());
            drawSuggestionsTiles(editedPane, editedRoom, AICoCreator.getInstance().GetContributions());
            drawTintAiPlacedTiles(editedPane.getMap());
            System.out.println("UPDATE ROOM");
            AICoCreator.getInstance().getCcRoomViewController().updateRoom(editedPane.getMap());
        });
    }

    /***
     * The Method that AI uses to display Suggestions in the room
     * @param
     */
    public void displaySuggestions(List<Tile> tiles, CCRoomViewController ccRoomViewController)
    {
        drawSuggestionsTiles(editedPane, editedPane.getMap(), tiles);

        Platform.runLater(() -> {
            mapIsFeasible(editedPane.getMap().isIntraFeasible());
            redrawPatterns(editedPane.getMap());
            redrawLocks(editedPane.getMap());
            drawTintAiPlacedTiles(editedPane.getMap());
            //ccRoomViewController.updateRoom(editedPane.getMap());
        });

    }

    /**
     * Toggles the display of patterns on top of the map.
     *
     * "Why is this public?",  you ask. Because of FXML's method binding.
     */
    public void togglePatterns(boolean toggle) { //FIXME: Add this as an event, received by EditedRoomStackPane
        redrawPatterns(editedPane.getMap());
        patternCanvas.setVisible(toggle);
    }

    public void toggleLocks(boolean toggle)
    {
        redrawLocks(editedPane.getMap());
        lockCanvas.setVisible(toggle);
    }

    public void ping(PCGEvent e)
    {
        if(initialized)
        {
            if(e instanceof InteractiveRoomBrushUpdated)
            {
                this.currentBrush = ((InteractiveRoomBrushUpdated) e).brush;
            }
            else if(e instanceof EditedRoomToggleLocks)
            {
                toggleLocks(((EditedRoomToggleLocks) e).toggle);
            }
            else if(e instanceof EditedRoomTogglePatterns)
            {
                togglePatterns(((EditedRoomTogglePatterns) e).toggle);
            }
        }
    }
}
