package de.tub.ise.ec;
import de.tub.ise.hermes.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

	static Scanner sc = new Scanner(System.in);
	static int port = 8080; // Master
	static String host = "3.120.37.153"; // Master
	static int selection;
	static DateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
	static String startTimefromClient;
	static String endTimefromClient;
	static List<String[]> timestampsArray = new ArrayList<String[]>();
	static List<String[]> clientAsyncList = new ArrayList<String[]>();


	/**
	 * a function to display a simple menu for client-user
	 */
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

	/**
	 * This is the main function which runs the client server and display the command-line interface
	 */
	public static void main(String[] args) {
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
				sendrequest("update", "Sync",100);

			}
			else if (selectionsync == 2) {
				sendrequest("update", "ASync",100);
			}

		}
		else if (selection == 4) {
			sendrequest("delete","Sync",2);
		}
	}


	/**
	 * prepare an arrayList with key, value, operationType and mode, then create the request and send it to master
	 * @param operationtype: the operation type { CRUD - Create / Read / Update / Delete }
	 * @param mode: the request type {  Sync / ASync }
	 * @param numrequest: how many requests the client will send to the our system
	 */
	public static void sendrequest(String operationtype,String mode,int numrequest) {
		ArrayList<String> item = new ArrayList<>();
		item.add("monkey"); //Key
		item.add("Ahmad"); //Value
		item.add(operationtype);
		item.add(mode);
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			Response res;
			int count = 1;
			@Override
			public void run() {
				if(count == 1) {
					timestampsArray.add(new String[] {
							"client_start,",
							"master_receiveTimestamp,",
							"master_beforeSendRequestTimestamp,",
							"master_beforeSendBackTimestamp,",
							"slave_receiveTimestamp,",
							"slave_beforeSendBackTimestamp,",
							"client_get_response"
					});
					clientAsyncList.add(new String[] {
							"client_start,",
							"client_end"
					});
				}
				if (count == numrequest) {
					try {
						createFile("sync_timestamps",timestampsArray);
						createFile("async_client_timestamps",clientAsyncList);
						return;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				Request req = new Request(item, "serverHandlerID", "localClient");
				req.setOriginator("Client");
				Sender sender = new Sender(host, port);
				Date beforeDate = new Date();
				startTimefromClient = sdf.format(beforeDate);
				System.out.println("Start Client" + startTimefromClient);
				if(mode.equals("Sync")) {
					res = sender.sendMessage(req, 50000);
					System.out.println(res.getResponseMessage());
				}
				else if(mode.equals("ASync")) {
					res = sender.sendMessage(req, 50000);
					writeAsyncClientTimestamp();
					System.out.println(res.getResponseMessage());
				}
				if(operationtype.equals("update") && mode.equals("Sync")) {
					benchmarkUpdate(res,startTimefromClient);
				}
				count = count + 1;
			}
		}, 0, 1000);
	}

	/**
	 * store the client timestamps for the Async request
	 */
	private static void writeAsyncClientTimestamp() {
		Date afterDate = new Date();
		endTimefromClient = sdf.format(afterDate);
		System.out.println("End Client" + endTimefromClient);
		clientAsyncList.add(new String[] {
				startTimefromClient + ",",
				endTimefromClient
		});
	}

	/**
	 * get timestamps values and store them in an arrayList
	 * @param res: the response object which is coming from master
	 * @param startTimefromClient: timestamp at the client before send request to master
	 */
	public static void benchmarkUpdate(Response res,String startTimefromClient) {
		Date afterDate = new Date();
		endTimefromClient = sdf.format(afterDate);
		System.out.println("End Client" + endTimefromClient);
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

	/**
	 * create file at client with all timestamps only when the request mode is Sync
	 * @param file: the name of the file
	 * @param array: the arrayList which will be stored as a record in this csv file
	 * @throws IOException
	 */
	public static void createFile(String file,List<String[]> array) throws IOException {
		Date afterDate = new Date();
		String date = sdf.format(afterDate);
		FileWriter writer = new FileWriter(file + ".csv");
		int size = array.size();
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
