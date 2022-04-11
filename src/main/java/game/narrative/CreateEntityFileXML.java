package game.narrative;


import game.narrative.entity.Entity;
import game.narrative.entity.XML_Entity_Save_Object;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;


public class CreateEntityFileXML {
    private static String filePath;

    public static void NewEntityFile(Entity entity){
        XML_Entity_Save_Object o1 = new XML_Entity_Save_Object(entity);
        String test = o1.getName();

        try{
            FileOutputStream fos = new FileOutputStream(new File("./entityXML.xml"));
            XMLEncoder encoder = new XMLEncoder(fos);
            encoder.writeObject(o1);
            encoder.close();
            fos.close();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }



/*        try{
            FileInputStream fis = new FileInputStream(new File("./entityXML.xml"));
            XMLDecoder decoder = new XMLDecoder(fis);

            XML_Entity_Save_Object o2 = (XML_Entity_Save_Object) decoder.readObject();
            decoder.close();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }*/
    }

}
