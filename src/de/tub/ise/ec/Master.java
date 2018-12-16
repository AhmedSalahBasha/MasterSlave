package de.tub.ise.ec;

import de.tub.ise.hermes.Receiver;
import de.tub.ise.hermes.RequestHandlerRegistry;

import java.io.IOException;

public class Master { // Receiver

    static int port = 8080;

    /**
     * A simple main function to run the master server machine
     */
    public static void main(String[] args) {
        RequestHandlerRegistry reg = RequestHandlerRegistry.getInstance();
        reg.registerHandler("serverHandlerID", new MasterHandler());
            try {
            Receiver receiver = new Receiver(port);
            receiver.start();
            System.out.println("Master has started ...");
        } catch (IOException e) {
            System.out.println("Connection error: " + e);
        }
    }
}
