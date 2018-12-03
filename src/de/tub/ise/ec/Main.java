package de.tub.ise.ec;
import de.tub.ise.hermes.*;

import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {
		int port = 8888;
		String host = "127.0.0.2"; // localhost
		String target_id = "targetID";

		ArrayList<String> item = new ArrayList<>();
		item.add("monkey");
		item.add("banana");
		// Client: create request
		Request req = new Request(item, target_id, "localClient");


		Sender sender = new Sender(host, port);

		Response res = sender.sendMessage(req, 5000);

		System.out.println(res.responseCode());
		System.out.println(res.getResponseMessage());
//		System.out.println("Received: " + res.getResponseMessage());
		System.out.println("ITEMS: " + res.getItems());
	}
}
