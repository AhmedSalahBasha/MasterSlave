package de.tub.ise.ec;
import com.sun.jmx.snmp.Timestamp;
import de.tub.ise.hermes.*;

import java.io.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

	public static void main(String[] args) {

		int port = 8888;
		String host = "127.0.0.1"; // localhost

		//Pattern for showing milliseconds in the time "SSS"
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

		ArrayList<String> item = new ArrayList<>();
		item.add("monkey"); //Key
		item.add("banana"); //Value
		item.add("create"); //action { CRUD - Create / Read / Update / Delete }

		// Client: create request
		Request req = new Request(item, "serverHandlerID", "localClient");

		// Client: send request
		Sender sender = new Sender(host, port);

		//Sending message Asynchronously
//		ServerAsyncClass async = new ServerAsyncClass();
//		boolean callbackReturn = sender.sendMessageAsync(req, async);


		// create a List which contains Timestamps Array
		List<String[]> timestampsArray = new ArrayList<String[]>();
		timestampsArray.add(new String[] { "Id,", "client_start,", "master_receiveTimestamp,", "master_beforeSendRequestTimestamp,", "master_beforeSendBackTimestamp,", "slave_receiveTimestamp,", "slave_beforeSendBackTimestamp,", "client_get_response" });


		Timer t = new Timer();
		t.schedule(new TimerTask() {
			int count = 1;
			@Override
			public void run() {
				if (count == 5) {
					try {
						creatFile("file",timestampsArray);
						return;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				//Using Date class
				Date beforeDate = new Date();
				String beforeTimestamp = sdf.format(beforeDate);

				//Sending message Synchronously
				Response res = sender.sendMessage(req, 5000);

				System.out.println(res.getResponseMessage());
				Date afterDate = new Date();
				String afterTimestamp = sdf.format(afterDate);

				List<Serializable> responseList = res.getItems();
				String master_receiveTimestamp = responseList.get(0).toString();
				String master_beforeSendRequestTimestamp = responseList.get(1).toString();
				String master_beforeSendBackTimestamp = responseList.get(2).toString();
				String slave_receiveTimestamp = responseList.get(3).toString();
				String slave_beforeSendBackTimestamp = responseList.get(4).toString();
				timestampsArray.add(new String[] {
						Integer.toString(count) + ",",
						beforeTimestamp + ",",
						master_receiveTimestamp + ",",
						master_beforeSendRequestTimestamp + ",",
						master_beforeSendBackTimestamp + ",",
						slave_receiveTimestamp + ",",
						slave_beforeSendBackTimestamp + ",",
						afterTimestamp});
				count = count + 1;
			}


		}, 0, 1000);


		try {

//			PrintWriter writer = new PrintWriter("test.txt", "UTF-8");
//			for (int i = 0; i < timestampsArray.size(); i++) {
//				for (int j = 0; j < timestampsArray.get(i).length; j++) {
//					writer.println(timestampsArray.get(i)[j]);
//				}
//			}
//			writer.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void creatFile(String file,List<String[]> array) throws IOException {
		FileWriter writer = new FileWriter(file + ".csv");
		int size = array.size();
//		for (String[] str : array)
//		{
//			writer.write(str);
//		}
		for (int i = 0; i < array.size(); i++) {
				for (int j = 0; j < array.get(i).length; j++) {
					writer.write(array.get(i)[j]);
				}
			if (i < size - 1)
				writer.write("\n");
			}



		writer.close();
	}
}
