package de.tub.ise.ec;

import de.tub.ise.hermes.Receiver;
import de.tub.ise.hermes.RequestHandlerRegistry;

import java.io.IOException;

public class Slave { //Receiver

    static int port = 8080;

    public static void main(String[] args) {
        RequestHandlerRegistry reg = RequestHandlerRegistry.getInstance();
        reg.registerHandler("slaveHandlerID", new SlaveHandler());
        try {
            Receiver receiver = new Receiver(port);
            receiver.start();
            System.out.println("Slave has started ...");
        } catch (IOException e) {
            System.out.println("Connection error: " + e);
        }
    }
}
