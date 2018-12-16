package de.tub.ise.ec;

import de.tub.ise.ec.kv.FileSystemKVStore;
import de.tub.ise.ec.kv.KeyValueInterface;
import de.tub.ise.hermes.IRequestHandler;
import de.tub.ise.hermes.Request;
import de.tub.ise.hermes.Response;
import de.tub.ise.hermes.Sender;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MasterHandler implements IRequestHandler {
    static int port = 8080;
    static String host = "18.185.137.85"; // slave
    static DateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    static String receiveTimestamp;
    static String beforeSendRequestTimestamp;
    static List <String[]> timestampsArray = new ArrayList<String[]>();
    static int id = 0 ;
    static String mod ="";
    static List<Serializable> serverTimestampList ;//= new ArrayList<>();
    static Response response;

    /**
     * this handelRequest function is overridden the original function in hermes
     * it takes the request object as a parameter, then it saves the timestamp once it receives the request,
     * then checks the type of operation {CRUD}, then it execute the operation on the hard-disk with key and value,
     * then it calls the function sendRequestToSlave to decide whether
     * to send a request synchronous or asynchronous to slave,
     * then it create a response and send back an arrayList of all timestamps inside Master and Slave to the Client
     * @param req: an arrayList coming from client has the Key, Value, operationType and requestType
     * @return a response object to client with an arrayList of all timestamps and a response message
     */
    @Override
    public Response handleRequest(Request req) {
        serverTimestampList = new ArrayList<>();
        Date receiveDate = new Date();
        receiveTimestamp = sdf.format(receiveDate);
        KeyValueInterface store = new FileSystemKVStore(".//master/");
        List<Serializable> list = req.getItems();
        for (Serializable s : list) {
            mod = ((ArrayList) s).get(3).toString();
            if (((ArrayList) s).get(2).toString().equals("create")) {
                store.store(((ArrayList) s).get(0).toString(), ((ArrayList) s).get(1).toString());
                sendRequestToSlave(req);
                response = new Response("File has been stored on Master successfully with value: " + store.getValue(((ArrayList) s).get(0).toString()),true,req,serverTimestampList);
                break;
            } else if (((ArrayList) s).get(2).toString().equals("read")) {
                Object valuesObject = store.getValue(((ArrayList) s).get(0).toString());System.out.println("Value is :  " + valuesObject.toString());
                response = new Response("Value is :  " + valuesObject.toString(),true,req,serverTimestampList);
                break;
            } else if (((ArrayList) s).get(2).toString().equals("update")) {
                store.update(((ArrayList) s).get(0).toString(), ((ArrayList) s).get(1).toString());
                sendRequestToSlave(req);
                response = new Response("File has been updated on Master successfully with value: " + store.getValue(((ArrayList) s).get(0).toString()),true,req,serverTimestampList);
                break;
            } else if (((ArrayList) s).get(2).toString().equals("delete")) {
                store.delete(((ArrayList) s).get(0).toString());
                sendRequestToSlave(req);
                response = new Response("File has been deleted on Master successfully!",true,req,serverTimestampList);
                break;
            }
        }
        return response;
    }

    /**
     *a simple function to save the timestamp directly after executing the operation on hard-disk
     * then it creates a request and assign this request to a specific handler and give this request an ID by setOriginator function
     * then it checks the requestType whether it's Async or Sync, then call another function to send the request
     * @param req an arrayList coming from client has the Key, Value, operationType and requestType
     */
    public void sendRequestToSlave(Request req) {
        Date beforeSendRequestDate = new Date();
        beforeSendRequestTimestamp = sdf.format(beforeSendRequestDate);
        Request slaveRequest = new Request(req.getItems(), "slaveHandlerID", "server");
        slaveRequest.setOriginator("Master");
        Sender sender = new Sender(host, port);
        Response slaveResponse = null;
        if( mod.equals("Sync"))
        {
            slaveResponse = syncslave(sender,slaveRequest);
            prepareDates(slaveResponse);
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

    /**
     * this function send the request Synchronously from Master to Slave
     * @param sender the sender object which has the host and port of the destination (slave)
     * @param slaveRequest the request object which has the the key, value and the operationType
     * @return the response back to master after finish all operations at Slave
     */
    public Response syncslave(Sender sender,Request slaveRequest) {
        Response slaveResponse = sender.sendMessage(slaveRequest, 5000);
        return  slaveResponse;
    }

    /**
     * it creates a new instance from SlaveAsync class which has the callback function which has the
     * response object coming from Slave
     * this function send the request ASynchronously from Master to Slave
     * @param sender the sender object which has the host and port of the destination (slave)
     * @param slaveRequest the request object which has the the key, value and the operationType
     */
    public void ASyncslave(Sender sender,Request slaveRequest) {
        SlaveAsync async = new SlaveAsync();
        boolean responseb = sender.sendMessageAsync(slaveRequest,async);
    }

    /**
     * this helper function is to save the timestamps before send back the response to Client
     * and it store all timestamps on Slave and Master in one arrayList and prepare a header for an excel sheet
     * then it calls the function createFile to save the file as masterAsync.csv and pass the whole arrayList to it.
     * @param slaveResponse is the response object coming from Slave which has the arrayList of all timestamps on Slave
     */
    public static void prepareDates(Response slaveResponse) {
        id++;
        Date beforeSendBackDate = new Date();
        String beforeSendBackTimestamp = sdf.format(beforeSendBackDate);
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
                createFile("masterAsync",timestampsArray);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * a simple function to create a file and store it on the hard-disk
     * @param file is the name of the file
     * @param array is the arrayList which will be saved as a record inside the CSV file
     * @throws IOException
     */
    public static void createFile(String file,List<String[]> array) throws IOException {
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