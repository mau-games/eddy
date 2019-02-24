package gui.controls;

import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;
import util.eventrouting.EventRouter;
import util.eventrouting.events.MAPEGridUpdate;
import util.eventrouting.events.SuggestedMapsLoading;

public class DimensionsTable extends TableView<MAPEDimensionFXML>
{
	
	private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
	
	public DimensionsTable()
	{
		super();
	}
	
	public void setup()
	{
		this.setPrefWidth(200);
		this.setMinHeight(10);
		this.setMaxHeight(150);
		this.setEditable(true);
		
		TableColumn<MAPEDimensionFXML, Integer> col1 = (TableColumn<MAPEDimensionFXML, Integer>)this.getColumns().get(0);
		TableColumn<MAPEDimensionFXML, Integer> col2 = (TableColumn<MAPEDimensionFXML, Integer>)this.getColumns().get(1);
		
		
		col1.prefWidthProperty().bind(this.widthProperty().multiply(0.75));
        col2.prefWidthProperty().bind(this.widthProperty().multiply(0.23));
        col1.setResizable(false);
        col2.setResizable(false);

        col2.setCellValueFactory(new Callback<CellDataFeatures<MAPEDimensionFXML, Integer>, ObservableValue<Integer>>() {
		    @Override
		    public ObservableValue<Integer> call(CellDataFeatures<MAPEDimensionFXML, Integer> p) {
		        return p.getValue().granularity.asObject();
		} 
		});
        
        col2.setCellFactory(TextFieldTableCell.<MAPEDimensionFXML, Integer>forTableColumn(new IntegerStringConverter()));
	}
	
	public void InitMainTable(MAPEVisualizationPane MAPEPane)
	{
		//Creisi thing
        ObservableList<MAPEDimensionFXML> data = FXCollections.observableArrayList(dim ->
        new Observable[] {
        		dim.granularity,
        		dim.dimension
        });
        
        data.addListener((Change<? extends MAPEDimensionFXML> c) -> {
        	
        	//This is jävla creisi
        	if(data.size() >= 2)
        	{
        		EventRouter.getInstance().postEvent(new MAPEGridUpdate(new MAPEDimensionFXML[] {data.get(0), data.get(1)}));
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
                new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
                new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5)
        );
        
        this.setItems(data);
	}
	
	public void setEventListeners()
	{
		this.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(SERIALIZED_MIME_TYPE)) 
            {
            	event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                event.consume();
            }
        });

		this.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);

                TableRow<MAPEDimensionFXML> tr = (TableRow<MAPEDimensionFXML>)event.getGestureSource();
               
                MAPEDimensionFXML draggedDimension = (MAPEDimensionFXML) tr.tableViewProperty().get().getItems().remove(draggedIndex); 
                int dropIndex = this.getItems().size();

                this.getItems().add(dropIndex, draggedDimension);
                event.setDropCompleted(true);
                this.getSelectionModel().select(dropIndex);
                event.consume();
            }
        });


		this.setRowFactory(tv -> {
            TableRow<MAPEDimensionFXML> row = new TableRow<>();

            row.setOnDragDetected(event -> {
                if (! row.isEmpty()) {
                    Integer index = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(SERIALIZED_MIME_TYPE, index);
                    db.setContent(cc);
                    event.consume();
                }
            });

            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    if (row.getIndex() != ((Integer)db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        event.consume();
                    }
                }
            });

            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
                    TableRow<MAPEDimensionFXML> tr = (TableRow<MAPEDimensionFXML>)event.getGestureSource();
                    MAPEDimensionFXML draggedDimension = (MAPEDimensionFXML) tr.tableViewProperty().get().getItems().remove(draggedIndex);

                    int dropIndex ; 

                    if (row.isEmpty()) {
                        dropIndex = this.getItems().size() ;
                    } else {
                        dropIndex = row.getIndex();
                    }

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
