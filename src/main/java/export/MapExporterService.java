package export;


import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import export.models.DungeonModel;
import export.models.SimpleXMLclass;


public final class MapExporterService {
	
	private MapExporterService() {};
	
	public static void SerializeDungeonToXML (String fileDestination, DungeonModel d) throws JAXBException
	{
		Marshaller marshaller = (JAXBContext.newInstance(new Class[] {DungeonModel.class})).createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT ,true);
		//marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.w3.org/2001/XMLSchema-instance");
		marshaller.marshal(d, new File(fileDestination));
		
		
		//Marshaller marshaller = (JAXBContext.newInstance(SimpleXMLclass.class)).createMarshaller();
		//marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT ,true);
		//marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.w3.org/2001/XMLSchema-instance");
		//marshaller.marshal(new SimpleXMLclass(5), new File(fileDestination));
	}
	
}

