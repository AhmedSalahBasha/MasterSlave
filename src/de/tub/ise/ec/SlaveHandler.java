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
    static String  beforeSendBackTimestamp;
    static DateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    static String receiveTimestamp;
    static Response response;
    static List<Serializable> slaveTimestampList ;
    @Override
    public Response handleRequest(Request req) {

        slaveTimestampList = new ArrayList<>();
        //Using Date class
        Date receiveDate = new Date();
        //Pattern for showing milliseconds in the time "SSS"
        receiveTimestamp = sdf.format(receiveDate);
        System.out.println("START Slave: " + receiveTimestamp);
        KeyValueInterface store = new FileSystemKVStore(".//slave/");
        List<Serializable> list = req.getItems();
    if(req.getOriginator().equals("Master"))
    {
        for (Serializable s : list) {
            if (((ArrayList) s).get(2).toString().equals("create")) {
                store.store(((ArrayList) s).get(0).toString(), ((ArrayList) s).get(1).toString());
                System.out.println("File has been stored on Slave successfully with value: " + store.getValue(((ArrayList) s).get(0).toString()));
                preparedates();
                response = new Response("That's a response message for target: " + req.getTarget() + "|| And the Slave Timestamp is: " + beforeSendBackTimestamp, true, req, slaveTimestampList);
            } else if (((ArrayList) s).get(2).toString().equals("update")) {
                store.update(((ArrayList) s).get(0).toString(), ((ArrayList) s).get(1).toString());
                //   System.out.println("File has been updated on Master successfully with value: " + store.getValue(((ArrayList) s).get(0).toString()));
                preparedates();
                response = new Response("That's a response message for target: " + req.getTarget() + "|| And the Slave Timestamp is: " + beforeSendBackTimestamp, true, req, slaveTimestampList);

                break;
            } else if (((ArrayList) s).get(2).toString().equals("delete")) {
                store.delete(((ArrayList) s).get(0).toString());
                System.out.println("File has been deleted on Slave successfully!");
                preparedates();
                response = new Response("That's a response message for target: " + req.getTarget() + "|| And the Slave Timestamp is: " + beforeSendBackTimestamp, true, req, slaveTimestampList);
                break;
            }
        }

    }
    else if (req.getOriginator().equals("Client"))
        {
            for (Serializable s : list) {

                if (((ArrayList) s).get(2).toString().equals("read")) {
                    Object valuesObject = store.getValue(((ArrayList) s).get(0).toString());
                    System.out.println("Value is :  " + valuesObject.toString());
                    preparedates();
                    response = new Response("Value is :  " + valuesObject.toString(), true, req, slaveTimestampList);
                    break;
                }
            }
        }

        System.out.println("After Commit Slave: " + beforeSendBackTimestamp);
    return response;

    }

    @Override
    public boolean requiresResponse() {
        return true;
    }

    public static void preparedates()
    {
        Date beforeSendBackDate = new Date();
        beforeSendBackTimestamp = sdf.format(beforeSendBackDate);
        slaveTimestampList.add(receiveTimestamp);
        slaveTimestampList.add(beforeSendBackTimestamp);
    }
}
