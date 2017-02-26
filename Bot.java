/*
   Allison Paul, Samuel Hsiang, Vaughan McDonald
   */

import java.lang.*;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;

public class Bot
{
	// connection to server
	public static Socket skt;
	public static BufferedReader from_exchange;
	public static PrintWriter to_exchange;

	public static void main(String[] args)
	{
		// setting up connection
		try
		{
			skt = new Socket("test-exch-same", 20000);
			from_exchange = new BufferedReader(new InputStreamReader(skt.getInputStream()));
			to_exchange = new PrintWriter(skt.getOutputStream(), true);

			to_exchange.println("HELLO SAME");
			String reply = from_exchange.readLine().trim();
			System.err.printf("The exchange replied: %s\n", reply);

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

	public static void handleIncomingMessages() {
		for(String message = from_exchange.nextLine(); message != null; message = from_exchange.nextLine()) {
			// do stuff
		}
	}

	public static void executeTrades() {
		// do stuff
	}
}

