package de.tub.ise.ec;

import com.sun.jmx.snmp.Timestamp;
import de.tub.ise.ec.kv.FileSystemKVStore;
import de.tub.ise.ec.kv.KeyValueInterface;
import de.tub.ise.hermes.IRequestHandler;
import de.tub.ise.hermes.Request;
import de.tub.ise.hermes.Response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SampleMessageHandler implements IRequestHandler {

    @Override
    public Response handleRequest(Request req) {

        KeyValueInterface store = new FileSystemKVStore();

        List<Serializable> list = req.getItems();
        for (Serializable s : list) {
            store.store(((ArrayList) s).get(0).toString(), ((ArrayList) s).get(1).toString());
        }
        System.out.println("File has been stored successfully with value: " + store.getValue("monkey"));
        store.delete("monkey");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return new Response("That's a response message for target: " + req.getTarget() + " || Server Timestamp >> " + timestamp, true, req, req.getItems());
    }

    @Override
    public boolean requiresResponse() {
        return true;
    }
}