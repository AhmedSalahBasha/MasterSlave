package de.tub.ise.ec;
import com.sun.jmx.snmp.Timestamp;
import de.tub.ise.hermes.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {

	static Scanner sc = new Scanner(System.in);
	static int port = 8888; // Server
	static String host = "127.0.0.1"; // Server
	static int selection;
	static DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	static String startTimefromClient;
	static String endTimefromClient;
	static List<String[]> timestampsArray = new ArrayList<String[]>();
	public static void displayMenu() {
		System.out.println("\nCRUD operations: \n \n"
				+ "1. Create \n"
				+ "2. Read \n"
				+ "3. Update\n \n"
				+ "4. Delete\n \n"
				+ "5. Exit\n \n"
				+ "Enter selection: ");
		selection = sc.nextInt(); // assign the user's input to the selection variable

	}
	public static void main(String[] args)
	{


		displayMenu();
		if (selection == 1) {
			sendrequest("create","Sync",2);

		} else if (selection == 2) {
			sendrequest("read","Sync",2);
		}
		else if (selection == 3) {
			System.out.println("\nplease Sync or ASync request : \n \n"
					+ "1. Sync \n"
					+ "2. Async \n"
					+ "Enter selection: ");
			int selectionsync = sc.nextInt();
			if ( selectionsync == 1) {
				sendrequest("update", "Sync",5);

			}
			else if (selectionsync == 2)
			{
				sendrequest("update", "ASync",5);
			}

		}
		else if (selection == 4) {
			sendrequest("delete","Sync",2);

		}

	}
	public static void sendrequest(String operationtype,String mod,int numrequest)
	{

		ArrayList<String> item = new ArrayList<>();
		item.add("monkey"); //Key
		item.add("Ahmad"); //Value
		item.add(operationtype); //action { CRUD - Create / Read / Update / Delete }
		item.add(mod);//action {  Sync / ASync }



		Timer t = new Timer();
		t.schedule(new TimerTask() {
			Response res;
			int count = 1;
			@Override
			public void run() {
				if(count == 1)
				{

					timestampsArray.add(new String[] {"client_start,", "master_receiveTimestamp,", "master_beforeSendRequestTimestamp,", "master_beforeSendBackTimestamp,", "slave_receiveTimestamp,", "slave_beforeSendBackTimestamp,", "client_get_response" });
				}
				if (count == numrequest) {
					try {
						creatFile("file",timestampsArray);
						return;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				// Client: create request
				Request req = new Request(item, "serverHandlerID", "localClient");
				req.setOriginator("Client");
				// Client: send request
				Sender sender = new Sender(host, port);
				//Pattern for showing milliseconds in the time "SSS"
				//Using Date class
				Date beforeDate = new Date();
				startTimefromClient = sdf.format(beforeDate);
				System.out.println("Start Client" + startTimefromClient);
				if(mod.equals("Sync"))
				{
					//Sending message Synchronously
					res = sender.sendMessage(req, 5000);
					System.out.println(res.getResponseMessage());
				}
				else if(mod.equals("ASync"))
				{
					res = sender.sendMessage(req, 5000);
					System.out.println(res.getResponseMessage());
					//Sending message Asynchronously
						//ServerAsyncClass async = new ServerAsyncClass();
						//boolean callbackReturn = sender.sendMessageAsync(req, async);
					// create a List which contains Timestamps Array
				}
				//System.out.println(res.getResponseMessage());
				Date afterDate = new Date();
				endTimefromClient = sdf.format(afterDate);
				System.out.println("End Client" + endTimefromClient);
				if(operationtype.equals("update") && mod.equals("Sync"))
				{
					benchmarkUpdate(res,startTimefromClient,endTimefromClient);
				}
				count = count + 1;
//				try {
//					TimeUnit.SECONDS.sleep(2);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			}
		}, 0, 1000);
	}
	public static void benchmarkUpdate(Response res,String startTimefromClient,String endTimefromClient)
	{
		List<Serializable> responseList = res.getItems();
		String master_receiveTimestamp = responseList.get(0).toString();
		String master_beforeSendRequestTimestamp = responseList.get(1).toString();
		String master_beforeSendBackTimestamp = responseList.get(2).toString();
		String slave_receiveTimestamp = responseList.get(3).toString();
		String slave_beforeSendBackTimestamp = responseList.get(4).toString();
		timestampsArray.add(new String[] {
				startTimefromClient + ",",
				master_receiveTimestamp + ",",
				master_beforeSendRequestTimestamp + ",",
				master_beforeSendBackTimestamp + ",",
				slave_receiveTimestamp + ",",
				slave_beforeSendBackTimestamp + ",",
				endTimefromClient});

	}

	public static void creatFile(String file,List<String[]> array) throws IOException {
		Date afterDate = new Date();
		String date = sdf.format(afterDate);
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
