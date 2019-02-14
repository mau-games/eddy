package export.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="SimpleXMLclass")
public class SimpleXMLclass {

	private int test;
	
	@XmlElement(name="Test")
	public int getTest() {
		return test;
	}

	public void setTest(int test) {
		this.test = test;
	}

	public SimpleXMLclass()
	{
		
	}
	public SimpleXMLclass(int test)
	{
		this.test = test;
	}
}
