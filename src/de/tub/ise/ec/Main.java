package de.tub.ise.ec;
import com.sun.jmx.snmp.Timestamp;
import de.tub.ise.hermes.*;

import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {

		int port = 8888;
		String host = "127.0.0.2"; // localhost

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println("Client Timestamp >> " + timestamp);
		ArrayList<String> item = new ArrayList<>();
		item.add("monkey");
		item.add("banana");

		// Client: create request
		Request req = new Request(item, "targetID", "localClient");

		// Client: send request
		Sender sender = new Sender(host, port);

		//Sending message Asynchronously
//		AsyncClass async = new AsyncClass();
//		boolean callbackReturn = sender.sendMessageAsync(req, async);

		Response res = sender.sendMessage(req, 5000);
		System.out.println(res.getResponseMessage());
	}
}
