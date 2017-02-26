/*
   Allison Paul, Samuel Hsiang, Vaughan McDonald
   */

import java.lang.*;
import java.io.*;
import java.net.Socket;

public class Bot
{
	// connection to server
	public static Socket skt;
	public static BufferedReader from_exchange;
	public static PrintWriter to_exchange;

	public static int identifier;

	public static void main(String[] args)
	{
		try
		{
			// initialization
			skt = new Socket("test-exch-same", 20000);
			from_exchange = new BufferedReader(new InputStreamReader(skt.getInputStream()));
			to_exchange = new PrintWriter(skt.getOutputStream(), true);

			to_exchange.println("HELLO SAME");
			String reply = from_exchange.readLine().trim();
			System.err.printf("The exchange replied: %s\n", reply);

			identifier = 1;

			// here temporarily
			to_exchange.println("ADD " + identifier + " BOND BUY 999 100");
			identifier++;
			to_exchange.println("ADD " + identifier + " BOND SELL 1001 100");
			identifier++;

			while(true) {
				try {
					handleIncomingMessages();
					executeTrades();
				} catch(Exception e) {
					e.printStackTrace(System.out);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
	}

	public static void handleIncomingMessages() throws IOException {
		for(String message = from_exchange.readLine(); message != null; message = from_exchange.readLine()) {
			// do stuff
			System.err.printf("The exchange replied: %s\n", message);
		}
	}

	public static void executeTrades() {
		// do stuff
	}
}

