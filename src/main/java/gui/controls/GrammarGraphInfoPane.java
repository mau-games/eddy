package gui.controls;

import javafx.beans.NamedArg;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;

public class GrammarGraphInfoPane extends VBox
{
    @FXML Text evaluation_title;
    @FXML
    public Text fitnessText;
    @FXML public Text NMesoText;
    @FXML public Text NMicroText;
    @FXML public Text lenText;
    @FXML public Text linText;
    @FXML public Text symText;

    public GrammarGraphInfoPane(@NamedArg("alignment") Pos alignment,
                                @NamedArg("prefWidth") double prefWidth, @NamedArg("minWidth") double minWidth,  @NamedArg("maxWidth") double maxWidth,
                                @NamedArg("prefHeight") double prefHeight, @NamedArg("minHeight") double minHeight,  @NamedArg("maxHeight") double maxHeight,
                                @NamedArg("spacing") double spacing)
    {
        super();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/controls/GrammarGraphInfo.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.setAlignment(alignment);
        this.setPrefWidth(prefWidth);
        this.setMinWidth(minWidth);
        this.setMaxWidth(maxWidth);
        this.setPrefHeight(prefHeight);
        this.setMinHeight(minHeight);
        this.setMaxHeight(maxHeight);
        this.setSpacing(spacing);
    }

    public GrammarGraphInfoPane(@NamedArg("alignment") Pos alignment,
                                @NamedArg("spacing") double spacing,
            @NamedArg("evaluation") String evaluation_text)
    {
        super();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/controls/GrammarGraphInfo.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.setAlignment(alignment);
        this.setSpacing(spacing);
        this.evaluation_title.setText(evaluation_text);
    }
}

