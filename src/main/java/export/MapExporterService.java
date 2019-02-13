package export;


import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import export.models.DungeonModel;


public final class MapExporterService {
	
	private MapExporterService() {};
	
	public static void SerializeDungeonToXML (String fileDestination, DungeonModel d) throws JAXBException
	{
		Marshaller marshaller = (JAXBContext.newInstance(new Class[] {DungeonModel.class})).createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT ,true);
		marshaller.marshal(d, new File(fileDestination));
	}
	
}

