package game.narrative;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.control.*;

public class NarrativeAttribute extends Defines{

    public enum AttributeTypes{
        Name,
        Age,
        Gender,
        Race,
        Class,
        Relationship
    }

    private VBox attributeGUI;
    public VBox getAttributeGUI() { return attributeGUI;}

    ItemType itemEnum;
    Class classEnum;
    Race raceEnum;
    Element elementEnum;
    RelationshipType relationEnum;

    private AttributeTypes attributeType;
    public AttributeTypes getType(){ return attributeType;}

    public NarrativeAttribute(AttributeTypes attribute){
        attributeType = attribute;
        attributeGUI = new VBox();

        attributeGUI.setPrefWidth(140);
        attributeGUI.setMaxWidth(200);
        attributeGUI.setMaxHeight(80);
        CreateAttributeGUI(attribute);
    }


    private void CreateAttributeGUI(AttributeTypes atr){
        //label
        Label atrLbl = new Label(atr.toString());
        atrLbl.setFont(Font.font(18.0));
        atrLbl.setTextFill(Color.WHITE);
        atrLbl.setStyle("-fx-background-color: transparent;");
        attributeGUI.getChildren().add(atrLbl);

        switch(attributeType){
            case Name:
                //textField
                TextField tf = new TextField();
                tf.setPrefWidth(80);
                tf.setMaxWidth(90);
                tf.setStyle("-fx-text-inner-color: white;");
                attributeGUI.getChildren().add( tf);
                break;
            case Age:
                //textfield
                TextField tf2 = new TextField();
                tf2.setPrefWidth(80);
                tf2.setMaxWidth(90);
                tf2.setStyle("-fx-text-inner-color: white;");
                attributeGUI.getChildren().add(tf2);
                break;
            case Gender:
                attributeGUI.getChildren().add(CreateGenderMenuGUI());
                break;
            case Race:
                attributeGUI.getChildren().add(CreateRaceMenuGUI());
                break;
            case Class:
                attributeGUI.getChildren().add(CreateClassMenuGUI());
                break;
            case Relationship:
                CreateRelationGUI();
                break;
            default:
                break;
        }

        attributeGUI.setStyle("-fx-border-radius: 10; -fx-border-color: #666;");
        attributeGUI.setAlignment(Pos.TOP_CENTER);
    }

    private MenuButton CreateGenderMenuGUI(){
        MenuButton mb = new MenuButton();
        mb.setPrefWidth(100);
        mb.setMaxWidth(120);

        mb.setText("gender");
        mb.setStyle("-fx-background-color: white;");

        for (Gender g : Gender.values()){
            if(g.toString() == "LOCKED")
                continue;

            MenuItem mi = new MenuItem();
            mi.setText(g.toString());

            mb.getItems().add(mi);
        }

        return mb;
    }

    private MenuButton CreateRaceMenuGUI(){
        MenuButton mb = new MenuButton();
        mb.setPrefWidth(100);
        mb.setMaxWidth(120);

        mb.setText("race");
        mb.setStyle("-fx-background-color: white;");


        for (Race r : Race.values()){
            if(r.toString() == "LOCKED")
                continue;

            MenuItem mi = new MenuItem();
            mi.setText(r.toString());
            mb.getItems().add(mi);
        }

        return mb;
    }

    private MenuButton CreateClassMenuGUI(){
        MenuButton mb = new MenuButton();
        mb.setPrefWidth(100);
        mb.setMaxWidth(120);

        mb.setText("class");
        mb.setStyle("-fx-background-color: white;");


        for (Class c : Class.values()){
            if(c.toString() == "LOCKED")
                continue;

            MenuItem mi = new MenuItem();
            mi.setText(c.toString());
            mb.getItems().add(mi);
        }

        return mb;
    }

    private HBox CreateHBoxInRelationGUI(){
        HBox hbox = new HBox();
        hbox.setSpacing(6);
        MenuButton mb = new MenuButton();
        mb.setPrefWidth(100);
        mb.setMaxWidth(120);


        for (RelationshipType r : RelationshipType.values()){
            if(r.toString() == "LOCKED")
                continue;

            MenuItem mi = new MenuItem();
            mi.setText(r.toString());
            mi.setId(r.toString());

            mi.setOnAction(event ->
            {
                if (hbox.getChildren().size() >= 3) {
                    return;
                }
                Label arrow = new Label("=>");
                arrow.setTextFill(Color.WHITE);
                arrow.setFont(Font.font(14.0));
                arrow.setStyle("-fx-background-color: transparent;");
                hbox.getChildren().add(hbox.getChildren().size() -1, arrow);

                if (mi.getId() == "Phobia") {
                    MenuButton mb2 = new MenuButton();
                    for (Element e : Element.values()) {
                        if (e.toString() == "LOCKED" || e.toString() == "NONE")
                            continue;

                        MenuItem mi2 = new MenuItem();
                        mi2.setText(e.toString());
                        mi2.setId(e.toString());

                        mb2.getItems().add(mi2);
                    }
                    mb2.setPrefWidth(70);
                    hbox.getChildren().add(hbox.getChildren().size() -1, mb2);
                }
                else {
                    ToggleButton entityBtn = new ToggleButton("select entity");

                    Label lbl = new Label('"' + "insert Enitity" + '"');
                    hbox.getChildren().add(hbox.getChildren().size() -1, lbl);
                }
            });
            mb.getItems().add(mi);
        }

        hbox.getChildren().add(mb);

        Button btn = new Button("-");
        btn.prefWidth(30);
        btn.prefHeight(30);
        btn.setStyle("-fx-background-color: red;");
        btn.setOnAction(event -> attributeGUI.getChildren().remove(hbox));
        hbox.getChildren().add(btn);

        return hbox;
    }

    private void CreateRelationGUI(){
        attributeGUI.setAlignment(Pos.CENTER_LEFT);
        attributeGUI.setMaxHeight(500);
        attributeGUI.setPrefWidth(300);
        attributeGUI.setPrefHeight(100);
        attributeGUI.setMaxWidth(350);

        attributeGUI.setSpacing(15);
        //Image image = new Image()
        //ImageView iv = new ImageView();
        HBox hbox = CreateHBoxInRelationGUI();

        attributeGUI.getChildren().add(hbox);

        javafx.scene.control.Button btn = new javafx.scene.control.Button("Add Relation");
        btn.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            attributeGUI.getChildren().add(attributeGUI.getChildren().size() - 1,   CreateHBoxInRelationGUI());
        });

        attributeGUI.getChildren().add( btn);
    }

}


