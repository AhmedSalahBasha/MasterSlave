package de.tub.ise.ec;

import com.sun.jmx.snmp.Timestamp;
import de.tub.ise.ec.kv.FileSystemKVStore;
import de.tub.ise.ec.kv.KeyValueInterface;
import de.tub.ise.hermes.IRequestHandler;
import de.tub.ise.hermes.Request;
import de.tub.ise.hermes.Response;
import de.tub.ise.hermes.Sender;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ServerHandler implements IRequestHandler {

    static int port = 8080;
    static String host = "127.0.0.2"; // localhost

    @Override
    public Response handleRequest(Request req) {

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
        // Server: create request
        Request slaveRequest = new Request(req.getItems(), "slaveHandlerID", "server");
        // Server: send request
        Sender sender = new Sender(host, port);
        //Sending message Synchronously
        Response slaveResponse = sender.sendMessage(slaveRequest, 5000);
        System.out.println(slaveResponse.getResponseMessage());

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return new Response("That's a response message for target: " + req.getTarget() + " || Server Timestamp >> " + timestamp, true, req, req.getItems());
    }

    @Override
    public boolean requiresResponse() {
        return true;
    }
}