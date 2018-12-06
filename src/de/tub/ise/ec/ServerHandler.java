package de.tub.ise.ec;

import com.sun.jmx.snmp.Timestamp;
import de.tub.ise.ec.kv.FileSystemKVStore;
import de.tub.ise.ec.kv.KeyValueInterface;
import de.tub.ise.hermes.IRequestHandler;
import de.tub.ise.hermes.Request;
import de.tub.ise.hermes.Response;
import de.tub.ise.hermes.Sender;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServerHandler implements IRequestHandler {

    static int port = 8080;
    static String host = "127.0.0.2"; // slave

    @Override
    public Response handleRequest(Request req) {

        //Using Date class
        Date receiveDate = new Date();
        //Pattern for showing milliseconds in the time "SSS"
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String receiveTimestamp = sdf.format(receiveDate);
        System.out.println("Server: Timestamp once received a Request from Client >> " + receiveTimestamp);

        KeyValueInterface store = new FileSystemKVStore();

        List<Serializable> list = req.getItems();
        for (Serializable s : list) {
            if (((ArrayList) s).get(2).toString().equals("create")) {
                store.store(((ArrayList) s).get(0).toString(), ((ArrayList) s).get(1).toString());
                System.out.println("File has been stored on Server successfully with value: " + store.getValue(((ArrayList) s).get(0).toString()));
                break;
            } else if (((ArrayList) s).get(2).toString().equals("read")) {
                Object valuesObject = store.getValue(((ArrayList) s).get(0).toString());
                System.out.println("Value is :  " + valuesObject.toString());
                break;
            } else if (((ArrayList) s).get(2).toString().equals("update")) {
                store.store(((ArrayList) s).get(0).toString(), ((ArrayList) s).get(1).toString());
                System.out.println("File has been updated on Server successfully with value: " + store.getValue(((ArrayList) s).get(0).toString()));
                break;
            } else if (((ArrayList) s).get(2).toString().equals("delete")) {
                store.delete(((ArrayList) s).get(0).toString());
                System.out.println("File has been deleted on Server successfully!");
                break;
            }
        }


        Date beforeSendRequestDate = new Date();
        String beforeSendRequestTimestamp = sdf.format(beforeSendRequestDate);
        System.out.println("Server: Timestamp BEFORE sending a NEW REQUEST to Slave >> " + beforeSendRequestTimestamp);
        // Server: create request
        Request slaveRequest = new Request(req.getItems(), "slaveHandlerID", "server");
        // Server: send request
        Sender sender = new Sender(host, port);

        //Sending message Asynchronously
//		SlaveAsyncClass async = new SlaveAsyncClass();
//		boolean callbackReturn = sender.sendMessageAsync(slaveRequest, async);

        //Sending message Synchronously
        Response slaveResponse = sender.sendMessage(slaveRequest, 5000);
        System.out.println(slaveResponse.getResponseMessage());

        Date beforeSendBackDate = new Date();
        String beforeSendBackTimestamp = sdf.format(beforeSendBackDate);
        List<Serializable> serverTimestampList = new ArrayList<>();
        serverTimestampList.add(receiveTimestamp);
        serverTimestampList.add(beforeSendRequestTimestamp);
        serverTimestampList.add(beforeSendBackTimestamp);
        serverTimestampList.addAll(slaveResponse.getItems());


//        System.out.println("Server: Timestamp BEFORE sending a Response back to client >> " + beforeSendBackTimestamp);
        return new Response("That's a response message for target: " + req.getTarget() + "|| And the Server Timestamp is: " + beforeSendBackTimestamp, true, req, serverTimestampList);
    }

    @Override
    public boolean requiresResponse() {
        return true;
    }
}