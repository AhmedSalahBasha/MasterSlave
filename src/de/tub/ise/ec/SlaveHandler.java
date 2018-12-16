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

    /**
     * this handelRequest function is overridden the original function in hermes
     * it takes the request object as a parameter, then it saves the timestamp once it receives the request,
     * then checks the type of operation {CRUD}, then it execute the operation on the hard-disk with key and value,
     * then it create a response and send back an arrayList of all timestamps inside Slave to the Master
     * @param req an arrayList coming from Master has the Key, Value, operationType
     * @return a response object to Master with an arrayList of all timestamps and a response message
     */
    @Override
    public Response handleRequest(Request req) {
        slaveTimestampList = new ArrayList<>();
        Date receiveDate = new Date();
        receiveTimestamp = sdf.format(receiveDate);
        System.out.println("START Slave: " + receiveTimestamp);
        KeyValueInterface store = new FileSystemKVStore(".//slave/");
        List<Serializable> list = req.getItems();
        if(req.getOriginator().equals("Master")) {
            for (Serializable s : list) {
                if (((ArrayList) s).get(2).toString().equals("create")) {
                    store.store(((ArrayList) s).get(0).toString(), ((ArrayList) s).get(1).toString());
                    prepareDates();
                    response = new Response("That's a response message for target: " + req.getTarget() + "|| And the Slave Timestamp is: " + beforeSendBackTimestamp, true, req, slaveTimestampList);
                } else if (((ArrayList) s).get(2).toString().equals("update")) {
                    store.update(((ArrayList) s).get(0).toString(), ((ArrayList) s).get(1).toString());
                    prepareDates();
                    response = new Response("That's a response message for target: " + req.getTarget() + "|| And the Slave Timestamp is: " + beforeSendBackTimestamp, true, req, slaveTimestampList);

                    break;
                }
                else if (((ArrayList) s).get(2).toString().equals("delete")) {
                    store.delete(((ArrayList) s).get(0).toString());
                    prepareDates();
                    response = new Response("That's a response message for target: " + req.getTarget() + "|| And the Slave Timestamp is: " + beforeSendBackTimestamp, true, req, slaveTimestampList);
                    break;
                }
            }
        }
        else if (req.getOriginator().equals("Client")) {
            for (Serializable s : list) {
                if (((ArrayList) s).get(2).toString().equals("read")) {
                    Object valuesObject = store.getValue(((ArrayList) s).get(0).toString());
                    prepareDates();
                    response = new Response("Value is :  " + valuesObject.toString(), true, req, slaveTimestampList);
                    break;
                }
            }
        }
        return response;
    }

    @Override
    public boolean requiresResponse() {
        return true;
    }

    /**
     * a simple function to save to timestamp before send back the response, and it store all timestamps in one arrayList
     */
    public static void prepareDates() {
        Date beforeSendBackDate = new Date();
        beforeSendBackTimestamp = sdf.format(beforeSendBackDate);
        slaveTimestampList.add(receiveTimestamp);
        slaveTimestampList.add(beforeSendBackTimestamp);
    }
}
