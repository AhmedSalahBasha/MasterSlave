package de.tub.ise.ec;

import com.sun.jmx.snmp.Timestamp;
import de.tub.ise.ec.kv.FileSystemKVStore;
import de.tub.ise.ec.kv.KeyValueInterface;
import de.tub.ise.hermes.IRequestHandler;
import de.tub.ise.hermes.Request;
import de.tub.ise.hermes.Response;
import de.tub.ise.hermes.Sender;
import jdk.internal.dynalink.beans.StaticClass;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServerHandler implements IRequestHandler {
    static int port = 8080;
    static String host = "127.0.0.2"; // slave
    static DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    static String receiveTimestamp;
    static String beforeSendRequestTimestamp;
    static List <String[]> timestampsArray = new ArrayList<String[]>();
    static int id = 0 ;
    static String mod ="";
    static List<Serializable> serverTimestampList ;//= new ArrayList<>();
    static Response response;
    @Override
    public Response handleRequest(Request req) {
        serverTimestampList = new ArrayList<>();
        //timestampsArray = new ArrayList<String[]>();
        //Using Date class
        Date receiveDate = new Date();
        //Pattern for showing milliseconds in the time "SSS"
        System.out.println("----------------------------------- ");
        receiveTimestamp = sdf.format(receiveDate);
        System.out.println("START MASTER: " + receiveTimestamp);
        KeyValueInterface store = new FileSystemKVStore(".//master/");
        List<Serializable> list = req.getItems();
        for (Serializable s : list) {
            mod = ((ArrayList) s).get(3).toString();
            if (((ArrayList) s).get(2).toString().equals("create")) {
                store.store(((ArrayList) s).get(0).toString(), ((ArrayList) s).get(1).toString());
                System.out.println();
                sendrequesttoslave(req);
                response = new Response("File has been stored on Server successfully with value: " + store.getValue(((ArrayList) s).get(0).toString()),true,req,serverTimestampList);
                break;
            } else if (((ArrayList) s).get(2).toString().equals("read")) {
                Object valuesObject = store.getValue(((ArrayList) s).get(0).toString());System.out.println("Value is :  " + valuesObject.toString());
                response = new Response("Value is :  " + valuesObject.toString(),true,req,serverTimestampList);
                break;
            } else if (((ArrayList) s).get(2).toString().equals("update")) {
                store.update(((ArrayList) s).get(0).toString(), ((ArrayList) s).get(1).toString());
               System.out.println("File has been updated on Server successfully with value: " + store.getValue(((ArrayList) s).get(0).toString()));
                sendrequesttoslave(req);
                response = new Response("File has been updated on Server successfully with value: " + store.getValue(((ArrayList) s).get(0).toString()),true,req,serverTimestampList);
                break;
            } else if (((ArrayList) s).get(2).toString().equals("delete")) {
                store.delete(((ArrayList) s).get(0).toString());
                System.out.println("File has been deleted on Server successfully!");
                sendrequesttoslave(req);
                response = new Response("File has been deleted on Server successfully!",true,req,serverTimestampList);
                break;
            }
        }
        return response;
    }
    // Server: create request
    public void sendrequesttoslave(Request req)
    {
        Date beforeSendRequestDate = new Date();
        beforeSendRequestTimestamp = sdf.format(beforeSendRequestDate);
        System.out.println("After Commit MASTER: " + beforeSendRequestTimestamp);
        Request slaveRequest = new Request(req.getItems(), "slaveHandlerID", "server");
        slaveRequest.setOriginator("Master");
        // Server: send request
        Sender sender = new Sender(host, port);
        Response slaveResponse = null;
        if( mod.equals("Sync"))
        {
            slaveResponse = syncslave(sender,slaveRequest);
            preparedates(slaveResponse);
        }
        else if (mod.equals("ASync"))
        {
            ASyncslave(sender,slaveRequest);
        }
    }
    @Override
    public boolean requiresResponse() {
        return true;
    }
    public Response syncslave(Sender sender,Request slaveRequest)
    {
        //Sending message Synchronously
         Response slaveResponse = sender.sendMessage(slaveRequest, 5000);
       // System.out.println(slaveResponse.getResponseMessage());
        return  slaveResponse;
    }
    public void ASyncslave(Sender sender,Request slaveRequest)
    {
        //Sending message Asynchronously
        SlaveAsyncClass async = new SlaveAsyncClass();
        Response response = null;
        boolean responseb = sender.sendMessageAsync(slaveRequest,async);

    }
    public static void preparedates(Response slaveResponse)
    {   id++;
        Date beforeSendBackDate = new Date();
        String beforeSendBackTimestamp = sdf.format(beforeSendBackDate);
        System.out.println(" Start Slave " + slaveResponse.getItems().get(0));
        System.out.println(" END Slave " + slaveResponse.getItems().get(1));
        System.out.println(" END MASTER(recieve Response from Slave): " + beforeSendBackTimestamp);
        System.out.println("----------------------------------- ");
        serverTimestampList.add(receiveTimestamp);
        serverTimestampList.add(beforeSendRequestTimestamp);
        serverTimestampList.add(beforeSendBackTimestamp);
        serverTimestampList.addAll(slaveResponse.getItems());
        if (mod.equals("ASync"))
        {
            if(id == 1)
            {
                timestampsArray.add (new String[] { "Id,", "Start Master,", "Commit Master,", "Start Slave,", "Commit Slave,", "End Master," });
            }
            timestampsArray.add(new String[]{
                    Integer.toString(id) + ",",
                    receiveTimestamp + ",",
                    beforeSendRequestTimestamp + ",",
                    slaveResponse.getItems().get(0) + ",",
                    slaveResponse.getItems().get(1) + ",",
                    beforeSendBackTimestamp + ","}
            );
            try {
                creatFile("masterAsync",timestampsArray);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void creatFile(String file,List<String[]> array) throws IOException {

        FileWriter writer = new FileWriter(file + ".csv");
        int size = array.size();
        for (int i = 0; i < array.size(); i++) {
            for (int j = 0; j < array.get(i).length; j++) {
                writer.write(array.get(i)[j]);
            }
            if (i < size - 1)
                writer.write("\n");
        }
        writer.close();
    }
}