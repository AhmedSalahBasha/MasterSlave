package de.tub.ise.ec;

import de.tub.ise.hermes.Receiver;
import de.tub.ise.hermes.RequestHandlerRegistry;

import java.io.*;

public class Server { // Receiver

    static int port = 8888;
    static String host = "127.0.0.2"; // localhost

    public static void main(String[] args) {
        RequestHandlerRegistry reg = RequestHandlerRegistry.getInstance();
        reg.registerHandler("targetID", new SampleMessageHandler());
            try {
            Receiver receiver = new Receiver(port);
            receiver.start();
        } catch (IOException e) {
            System.out.println("Connection error: " + e);
        }
    }
}
