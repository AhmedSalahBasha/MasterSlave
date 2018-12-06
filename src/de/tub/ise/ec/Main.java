package de.tub.ise.ec;
import com.sun.jmx.snmp.Timestamp;
import de.tub.ise.hermes.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Main {

	public static void main(String[] args) {

		int port = 8888;
		String host = "127.0.0.1"; // localhost

		//Using Date class
		Date beforeDate = new Date();
		//Pattern for showing milliseconds in the time "SSS"
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String beforeTimestamp = sdf.format(beforeDate);
		System.out.println("Client: Timestamp BEFORE Sending a request >> " + beforeTimestamp);
		ArrayList<String> item = new ArrayList<>();
		item.add("monkey"); //Key
		item.add("banana"); //Value
		item.add("create"); //action { CRUD - Create / Read / Update / Delete }

		// Client: create request
		Request req = new Request(item, "serverHandlerID", "localClient");

		// Client: send request
		Sender sender = new Sender(host, port);

		//Sending message Asynchronously
		ServerAsyncClass async = new ServerAsyncClass();
		boolean callbackReturn = sender.sendMessageAsync(req, async);

		//Sending message Synchronously
//		Response res = sender.sendMessage(req, 5000);
//		System.out.println(res.getResponseMessage());
		//Using Date class
		Date afterDate = new Date();
		//Pattern for showing milliseconds in the time "SSS"
		String afterTimestamp = sdf.format(afterDate);
		System.out.println("Client: Timestamp AFTER Received a response >> " + afterTimestamp);
	}
}
