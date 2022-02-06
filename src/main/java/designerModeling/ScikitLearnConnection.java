package designerModeling;

import game.Room;
import gui.utils.MapRenderer;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.RoomEdited;
import util.eventrouting.events.despers.DesPersEvaluation;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.nio.charset.StandardCharsets;

//import org.pmml4s.model.Model;
//import org.pmml4s.*;

public class ScikitLearnConnection implements Listener {

//    private final Model model = Model.fromFile(Main.class.getClassLoader().getResource("models/model_kmneans.pmml").getFile());

    private static ScikitLearnConnection instance = null;
    private static EventRouter router = EventRouter.getInstance();

    private ScikitLearnConnection()
    {
//        router.registerListener(this, new AlgorithmDone(null, null, null));
        router.registerListener(this, new RoomEdited(null));
        router.registerListener(this, new DesPersEvaluation(null));

    }

    /**
     * Gets the singleton instance of this class.
     *
     * @return An instance of MapRenderer.
     */
    public static ScikitLearnConnection getInstance() {
        if (instance == null) {
            instance = new ScikitLearnConnection();
        }
        return instance;
    }

    @Override
    public void ping(PCGEvent e)
    {
        if(e instanceof RoomEdited)
        {

        }
        else if(e instanceof  DesPersEvaluation)
        {

        }

    }

    public synchronized int[] getCluster(Room room)
    {
        int[] result = new int[3];

        return result;
    }

    public static void sendPOST()
    {
        try{
            URL url = new URL("http://127.0.0.1:5000/get_room/");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/xml");
            http.setRequestProperty("Accept", "application/xml");

            String data = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Request>\n    <Login>aeau</Login>\n    <Password>password</Password>\n</Request>";

            data = "<Room ID=\"6a14d5f3-12f0-4ee8-9ad8-c899d231bfe8\" height=\"7\" time=\"2019-10-25 10:24:44.273\" width=\"13\">\n" +
                    "<Dimensions>\n" +
                    "<Dimension name=\"SIMILARITY\" value=\"1.0\"/>\n" +
                    "<Dimension name=\"SYMMETRY\" value=\"-1.0\"/>\n" +
                    "</Dimensions>\n" +
                    "<Tiles>\n" +
                    "<Tile PosX=\"0\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"1\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"2\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"3\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"4\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"5\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"6\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"7\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"8\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"9\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"10\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"11\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"12\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"0\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"1\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"2\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"3\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"4\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"5\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"6\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"7\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"8\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"9\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"10\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"11\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"12\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"0\" PosY=\"2\" immutable=\"false\" value=\"WALL\"/>\n" +
                    "<Tile PosX=\"1\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"2\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"3\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"4\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"5\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"6\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"7\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"8\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"9\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"10\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"11\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"12\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"0\" PosY=\"3\" immutable=\"false\" value=\"DOOR\"/>\n" +
                    "<Tile PosX=\"1\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"2\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"3\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"4\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"5\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"6\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"7\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"8\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"9\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"10\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"11\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"12\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"0\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"1\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"2\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"3\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"4\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"5\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"6\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"7\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"8\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"9\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"10\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"11\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"12\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"0\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"1\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"2\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"3\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"4\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"5\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"6\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"7\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"8\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"9\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"10\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"11\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"12\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"0\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"1\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"2\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"3\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"4\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"5\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"6\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"7\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"8\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"9\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"10\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"11\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"12\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "</Tiles>\n" +
                    "<Customs/>\n" +
                    "</Room>";

            data = "<Room ID=\"6a14d5f3-12f0-4ee8-9ad8-c899d231bfe8\" height=\"7\" time=\"2019-10-25 10:24:52.339\" width=\"13\">\n" +
                    "<Dimensions>\n" +
                    "<Dimension name=\"SIMILARITY\" value=\"1.0\"/>\n" +
                    "<Dimension name=\"SYMMETRY\" value=\"-1.0\"/>\n" +
                    "</Dimensions>\n" +
                    "<Tiles>\n" +
                    "<Tile PosX=\"0\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"1\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"2\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"3\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"4\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"5\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"6\" PosY=\"0\" immutable=\"false\" value=\"WALL\"/>\n" +
                    "<Tile PosX=\"7\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"8\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"9\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"10\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"11\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"12\" PosY=\"0\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"0\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"1\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"2\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"3\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"4\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"5\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"6\" PosY=\"1\" immutable=\"false\" value=\"WALL\"/>\n" +
                    "<Tile PosX=\"7\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"8\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"9\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"10\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"11\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"12\" PosY=\"1\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"0\" PosY=\"2\" immutable=\"false\" value=\"WALL\"/>\n" +
                    "<Tile PosX=\"1\" PosY=\"2\" immutable=\"false\" value=\"WALL\"/>\n" +
                    "<Tile PosX=\"2\" PosY=\"2\" immutable=\"false\" value=\"WALL\"/>\n" +
                    "<Tile PosX=\"3\" PosY=\"2\" immutable=\"false\" value=\"WALL\"/>\n" +
                    "<Tile PosX=\"4\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"5\" PosY=\"2\" immutable=\"false\" value=\"WALL\"/>\n" +
                    "<Tile PosX=\"6\" PosY=\"2\" immutable=\"false\" value=\"WALL\"/>\n" +
                    "<Tile PosX=\"7\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"8\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"9\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"10\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"11\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"12\" PosY=\"2\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"0\" PosY=\"3\" immutable=\"false\" value=\"DOOR\"/>\n" +
                    "<Tile PosX=\"1\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"2\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"3\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"4\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"5\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"6\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"7\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"8\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"9\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"10\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"11\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"12\" PosY=\"3\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"0\" PosY=\"4\" immutable=\"false\" value=\"WALL\"/>\n" +
                    "<Tile PosX=\"1\" PosY=\"4\" immutable=\"false\" value=\"WALL\"/>\n" +
                    "<Tile PosX=\"2\" PosY=\"4\" immutable=\"false\" value=\"WALL\"/>\n" +
                    "<Tile PosX=\"3\" PosY=\"4\" immutable=\"false\" value=\"WALL\"/>\n" +
                    "<Tile PosX=\"4\" PosY=\"4\" immutable=\"false\" value=\"WALL\"/>\n" +
                    "<Tile PosX=\"5\" PosY=\"4\" immutable=\"false\" value=\"WALL\"/>\n" +
                    "<Tile PosX=\"6\" PosY=\"4\" immutable=\"false\" value=\"WALL\"/>\n" +
                    "<Tile PosX=\"7\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"8\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"9\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"10\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"11\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"12\" PosY=\"4\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"0\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"1\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"2\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"3\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"4\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"5\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"6\" PosY=\"5\" immutable=\"false\" value=\"WALL\"/>\n" +
                    "<Tile PosX=\"7\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"8\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"9\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"10\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"11\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"12\" PosY=\"5\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"0\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"1\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"2\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"3\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"4\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"5\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"6\" PosY=\"6\" immutable=\"false\" value=\"WALL\"/>\n" +
                    "<Tile PosX=\"7\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"8\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"9\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"10\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"11\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "<Tile PosX=\"12\" PosY=\"6\" immutable=\"false\" value=\"FLOOR\"/>\n" +
                    "</Tiles>\n" +
                    "<Customs/>\n" +
                    "</Room>";

            byte[] out = data.getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);

            System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
            http.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }


    }

    public static void main(String[] args)
    {
        sendPOST();

        HttpURLConnection conn = null;
        DataOutputStream os = null;
        try{
            URL url = new URL("http://127.0.0.1:5000/add/"); //important to add the trailing slash after add
//            ArrayList<String> asas = new ArrayList<String>(){add("SS"); add("3232");]
            ArrayList<String> inputData = new ArrayList<String>(){{
                add("{\"0\": 5, \"1\": 8, \"float\":\"0.5\", \"text\":\"random text\"}");
                    add("{\"0\":5, \"1\":14,  \"float\":\"0.5\", \"text\":\"testing\"}");
            }};

            String bigger = "{";

            for(int i = 0, j = 0; i < 10; i++, j+=2)
            {
                bigger += "\"" + i + "\": " + j + ", ";
            }

            bigger += " \"float\":\"0.5\", \"text\":\"LONGER!!\"}";

            inputData.add(bigger);

            System.out.println(inputData);

            for(String input: inputData){
                byte[] postData = input.getBytes(StandardCharsets.UTF_8);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty( "charset", "utf-8");
                conn.setRequestProperty("Content-Length", Integer.toString(input.length()));
                os = new DataOutputStream(conn.getOutputStream());
                os.write(postData);
                os.flush();

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));

                String output;
                System.out.println("Output from Server .... \n");
                while ((output = br.readLine()) != null) {
                    System.out.println(output);
                }
                conn.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }finally
        {
            if(conn != null)
            {
                conn.disconnect();
            }
        }
//
//        try {
//            URL url = new URL("http://127.0.0.1:5000/add?x=100&y=12&text='Test'");
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//            conn.setRequestProperty("Accept", "application/json");
//
//            if (conn.getResponseCode() != 200) {
//                throw new RuntimeException("Failed : HTTP error code : "
//                        + conn.getResponseCode());
//            }
//
//            BufferedReader br = new BufferedReader(new InputStreamReader(
//                    (conn.getInputStream())));
//
//            String output;
//            System.out.println("Output from Server .... \n");
//            while ((output = br.readLine()) != null) {
//                System.out.println(output);
//            }
//
//            conn.disconnect();
//
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//        Main main = new Main();
//        Map<String, Double> values = Map.of(
//                "age", 20d,
//                "sex", 1d,
//                "bmi", -100d,
//                "bp", -200d,
//                "s1", 1d,
//                "s2", 2d,
//                "s3", 3d,
//                "s4", 4d,
//                "s5", 5d,
//                "s6", 6d
//        );
//
//        double predicted = main.getRegressionValue(values);
//        System.out.println(predicted);
    }


}
