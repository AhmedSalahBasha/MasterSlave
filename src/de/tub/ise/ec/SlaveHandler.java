package de.tub.ise.ec;

import com.sun.jmx.snmp.Timestamp;
import de.tub.ise.ec.kv.FileSystemKVStore;
import de.tub.ise.ec.kv.KeyValueInterface;
import de.tub.ise.hermes.IRequestHandler;
import de.tub.ise.hermes.Request;
import de.tub.ise.hermes.Response;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SlaveHandler implements IRequestHandler {
    @Override
    public Response handleRequest(Request req) {

        //Using Date class
        Date receiveDate = new Date();
        //Pattern for showing milliseconds in the time "SSS"
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        String receiveTimestamp = sdf.format(receiveDate);
        System.out.println("Slave: Timestamp once received a Request from Server >> " + receiveTimestamp);

        KeyValueInterface store = new FileSystemKVStore();

        List<Serializable> list = req.getItems();
        for (Serializable s : list) {
            if (((ArrayList) s).get(2).toString().equals("create")) {
                store.store(((ArrayList) s).get(0).toString(), ((ArrayList) s).get(1).toString());
                System.out.println("File has been stored on Slave successfully with value: " + store.getValue(((ArrayList) s).get(0).toString()));
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
                System.out.println("File has been deleted on Slave successfully!");
                break;
            }
        }

        Date beforeSendBackDate = new Date();
        String beforeSendBackTimestamp = sdf.format(beforeSendBackDate);
        System.out.println("Slave: Timestamp Before send a response to Server >> " + beforeSendBackTimestamp);
        return new Response("That's a response message for target: " + req.getTarget(), true, req, req.getItems());
    }

    @Override
    public boolean requiresResponse() {
        return true;
    }
}
