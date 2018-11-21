package de.tub.ise.ec;

import de.tub.ise.ec.de.tub.ise.ec.kv.FileSystemKVStore;
import de.tub.ise.ec.de.tub.ise.ec.kv.KeyValueInterface;
import de.tub.ise.hermes.*;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        // KEY-VALUE STORE TEST
        String kvStorePath = "/Users/jacobeberhardt/Desktop/kv_store";
        // Test kv store
        KeyValueInterface store = new FileSystemKVStore(kvStorePath);
        store.store("affe","butterbrot");
        System.out.println("Received: " + store.getValue("affe"));
        store.delete("affe");

        // HERMES TEST
        // configure
        int port = 8080;
        String host = "127.0.0.1"; //localhost


        // register handler
        RequestHandlerRegistry reg = RequestHandlerRegistry.getInstance();
        reg.registerHandler("sampleMessageHandler", new SampleMessageHandler());

        // start receiver
        try {
            Receiver receiver = new Receiver(port);
            receiver.start();
        } catch (IOException e) {
            System.out.println("Connection error: " + e);
        }

        // send messages
        Sender sender = new Sender(host, port);

        Request req = new Request("Message","sampleMessageHandler","localSampleClient");

        Response res = sender.sendMessage(req, 5000);
        System.out.println("Received: " + res.getResponseMessage());

    }
}
