package gui.controls;

import game.Game;
import generator.algorithm.MAPElites.grammarDimensions.GADimensionGrammar;
import generator.algorithm.MAPElites.grammarDimensions.MAPEDimensionGrammarFXML;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;
import util.eventrouting.EventRouter;
import util.eventrouting.events.MAPEGridUpdate;
import util.eventrouting.events.MAPENarrativeGridUpdate;

public class NarrativeDimensionsTable extends TableView<MAPEDimensionGrammarFXML>
{
	public int maxSize = 0;

	public NarrativeDimensionsTable()
	{
		super();
	}
	
	public void setup(int size)
	{
		this.maxSize = size;
		
		this.setPrefWidth(200);
		this.setMinHeight(10);
		this.setMaxHeight(150);
		this.setEditable(true);
		
		TableColumn<MAPEDimensionGrammarFXML, Integer> col1 = (TableColumn<MAPEDimensionGrammarFXML, Integer>)this.getColumns().get(0);
		TableColumn<MAPEDimensionGrammarFXML, Integer> col2 = (TableColumn<MAPEDimensionGrammarFXML, Integer>)this.getColumns().get(1);
		
		
		col1.prefWidthProperty().bind(this.widthProperty().multiply(0.75));
        col2.prefWidthProperty().bind(this.widthProperty().multiply(0.23));
        col1.setResizable(false);
        col2.setResizable(false);

        col2.setCellValueFactory(new Callback<CellDataFeatures<MAPEDimensionGrammarFXML, Integer>, ObservableValue<Integer>>() {
		    @Override
		    public ObservableValue<Integer> call(CellDataFeatures<MAPEDimensionGrammarFXML, Integer> p) {
		        return p.getValue().granularity.asObject();
		} 
		});
        
        col2.setCellFactory(TextFieldTableCell.<MAPEDimensionGrammarFXML, Integer>forTableColumn(new IntegerStringConverter()));
	}
	
	public void InitMainTable(MAPEGrammarVisualizationPane MAPEPane)
	{
		//Creisi thing
        ObservableList<MAPEDimensionGrammarFXML> data = FXCollections.observableArrayList(dim ->
        new Observable[] {
        		dim.granularity,
        		dim.dimension
        });
        
        data.addListener((Change<? extends MAPEDimensionGrammarFXML> c) -> {
        	
        	//This is jävla creisi - but it works :P 
        	if(data.size() >= 2)
        	{
        		EventRouter.getInstance().postEvent(new MAPENarrativeGridUpdate(new MAPEDimensionGrammarFXML[] {data.get(0), data.get(1)}));
        	}
        	
            while (c.next()) {
            	
            	
                if (c.wasAdded()) {
                    System.out.println("Added:");
                    c.getAddedSubList().forEach(System.out::println);
                    System.out.println();
                }
                if (c.wasRemoved()) {
                    System.out.println("Removed:");
                    c.getRemoved().forEach(System.out::println);
                    System.out.println();
                }
                if (c.wasUpdated()) {
                    System.out.println("Updated:");
                    data.subList(c.getFrom(), c.getTo()).forEach(System.out::println);
                    System.out.println();
                }
            }
         });
        
        data.addAll(
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.CONFLICT, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.SIZE, 5)
        );
        
        this.setItems(data);
	}
	
	public void setEventListeners()
	{
		this.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(Game.SERIALIZED_MIME_TYPE))
            {
            	event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                event.consume();
            }
        });

		this.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(Game.SERIALIZED_MIME_TYPE)) {
                int draggedIndex = (Integer) db.getContent(Game.SERIALIZED_MIME_TYPE);

                TableRow<MAPEDimensionGrammarFXML> tr = (TableRow<MAPEDimensionGrammarFXML>)event.getGestureSource();
               
                MAPEDimensionGrammarFXML draggedDimension = (MAPEDimensionGrammarFXML) tr.tableViewProperty().get().getItems().remove(draggedIndex); 
                int dropIndex = this.getItems().size();
                
                if(this.getItems().size() + 1 > maxSize)
                {
                	if(dropIndex == this.getItems().size())
                	{
                    	tr.tableViewProperty().get().getItems().add(this.getItems().remove(this.getItems().size() - 1));
                    	dropIndex -= 1;
                	}
                	else
                	{
                    	tr.tableViewProperty().get().getItems().add(this.getItems().remove(dropIndex));
                	}
                }
                
                //Add the new dimension to the table
                this.getItems().add(dropIndex, draggedDimension);

                event.setDropCompleted(true);
                this.getSelectionModel().select(dropIndex);
                event.consume();
            }
        });


		this.setRowFactory(tv -> {
            TableRow<MAPEDimensionGrammarFXML> row = new TableRow<>();

            row.setOnDragDetected(event -> {
                if (! row.isEmpty()) {
                    Integer index = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(Game.SERIALIZED_MIME_TYPE, index);
                    db.setContent(cc);
                    event.consume();
                }
            });

            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(Game.SERIALIZED_MIME_TYPE)) {
                    if (row.getIndex() != ((Integer)db.getContent(Game.SERIALIZED_MIME_TYPE)).intValue()) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        event.consume();
                    }
                }
            });

            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(Game.SERIALIZED_MIME_TYPE)) {
                    int draggedIndex = (Integer) db.getContent(Game.SERIALIZED_MIME_TYPE);
                    TableRow<MAPEDimensionGrammarFXML> tr = (TableRow<MAPEDimensionGrammarFXML>)event.getGestureSource();
                    MAPEDimensionGrammarFXML draggedDimension = (MAPEDimensionGrammarFXML) tr.tableViewProperty().get().getItems().remove(draggedIndex);

                    int dropIndex ; 

                    if (row.isEmpty()) {
                        dropIndex = this.getItems().size() ;
                    } else {
                        dropIndex = row.getIndex();
                    }
//                    System.out.println(dropIndex);
                    //If more than 2 dimensions, we remove the top dimension :D and add it to the other list
                    if(this.getItems().size() + 1 > maxSize)
                    {
                    	if(dropIndex == this.getItems().size())
                    	{
                        	tr.tableViewProperty().get().getItems().add(this.getItems().remove(this.getItems().size() - 1));
                        	dropIndex -= 1;
                    	}
                    	else
                    	{
                        	tr.tableViewProperty().get().getItems().add(this.getItems().remove(dropIndex));
                    	}
                    }
                    
                    //Add the new dimension to the table
                    this.getItems().add(dropIndex, draggedDimension);
                   

                    event.setDropCompleted(true);
                    this.getSelectionModel().select(dropIndex);
                    event.consume();
                }
            });

            return row ;
        });
	}
	
}
