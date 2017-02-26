/*
   Allison Paul, Samuel Hsiang, Vaughan McDonald
   */

import java.lang.*;
import java.io.*;
import java.util.*;
import java.net.Socket;

public class Bot
{
	// connection to server
	public static Socket skt;
	public static BufferedReader from_exchange;
	public static PrintWriter to_exchange;

	// BOND		0
	// VALBZ	1
	// VALE		2
	// GS		3
	// MS		4
	// WFC		5
	// XLF		6

	public static int levels = new int[7];
	public static int buy_levels = new int[7];
	public static int sell_levels = new int[7];

	public static int usd = 0;

	public static int identifier;

	public static void main(String[] args)
	{
		try
		{
			// initialization
			// skt = new Socket("test-exch-same", 20000);
			skt = new Socket("production", 20000);
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
			String[] tokens = message.split();
			switch(tokens[0]) {
				case "ACK":
				case "REJECT":
					System.err.printf("The exchange replied: %s\n", message);
					break;
				case "FILL":
					System.err.printf("The exchange replied: %s\n", message);
					switch(tokens[2]) {
						case "BOND":
							switch(tokens[3]) {
								case "BUY":
								case "SELL":
							}
							break;
					}
					break;
				case "OUT":
					System.err.printf("The exchange replied: %s\n", message);
					break;
			}
		}
	}

	public static void executeTrades() {
		// do stuff
		System.err.printf("executing trades\n");
	}
}

