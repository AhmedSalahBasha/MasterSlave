package de.tub.ise.ec;

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

        List<Serializable> l = req.getItems();
        for (Serializable s : l) {
            System.out.println(s.getClass());

            store.store(((ArrayList) s).get(0).toString(), ((ArrayList) s).get(1).toString());
        }
        System.out.println("Received: " + store.getValue("monkey"));
        store.delete("monkey");
        System.out.println("-------------------------------------");
        System.out.println(req.getItems());
        return new Response("Echo okay for target: " + req.getTarget(), true, req, req.getItems());
    }

    @Override
    public boolean requiresResponse() {
        return true;
    }
}