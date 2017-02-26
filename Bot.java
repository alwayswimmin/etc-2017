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

	public static int[] levels = new int[7];
	public static int[] buy_levels = new int[7];
	public static int[] sell_levels = new int[7];
	public static int[] buys_sent = new int[7];
	public static int[] sells_sent = new int[7];

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
			
			for(int i = 0; i <7; i++){
// 				levels[i] = reply.substring(reply.indexOf(intToName(i))+reply.indexOf(intToName(i)).length(), reply.indexOf(" ", reply.indexOf(intToName(i))));
				levels[i] = Integer.parseInt(reply.substring(reply.indexOf(":", reply.indexOf(intToName(i)))+1, reply.indexOf(" ", reply.indexOf(intToName(i)))));
			}
			

			identifier = 1;

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

	public static int nameToInt(String s) {
		switch(s) {
			case "BOND":
				return 0;
			case "VALBZ":
				return 1;
			case "VALE":
				return 2;
			case "GS":
				return 3;
			case "MS":
				return 4;
			case "WFC":
				return 5;
			case "XLF":
				return 6;
			default:
				return -1;
		}
	}

	public static String intToName(int i) {
		switch(i) {
			case 0:
				return "BOND";
			case 1:
				return "VALBZ";
			case 2:
				return "VALE";
			case 3:
				return "GS";
			case 4:
				return "MS";
			case 5:
				return "WFC";
			case 6:
				return "XLF";
			default:
				return "";
		}
	}

	public static void handleIncomingMessages() throws IOException {
		for(String message = from_exchange.readLine(); message != null; message = from_exchange.readLine()) {
			String[] tokens = message.split(" ");
			switch(tokens[0]) {
				case "ACK":
				case "REJECT":
					System.err.printf("The exchange replied: %s\n", message);
					break;
				case "FILL":
					System.err.printf("The exchange replied: %s\n", message);
					int asset = nameToInt(tokens[2]);
					int price = Integer.parseInt(tokens[4]);
					int num = Integer.parseInt(tokens[5]);
					if(tokens[3].equals("BUY")) {
						levels[asset] += num;
						buys_sent[asset] -= num;
						usd -= num * price;
					} else {
						levels[asset] -= num;
						sells_sent[asset] -= num;
						usd += num * price;
					}
					break;
				case "OUT":
					System.err.printf("The exchange replied: %s\n", message);
					break;
			}
		}
	}

	public static void executeTrades() {
		// bonds
		int num_to_sell = 100 + levels[0] - sells_sent[0];
		int num_to_buy = 100 - levels[0] - buys_sent[0];
		if(num_to_buy > 0) {
			to_exchange.println("ADD " + identifier + " BOND BUY 999 " + num_to_buy);
			buys_sent[0] += num_to_buy;
			identifier++;
		}
		if(num_to_sell > 0) {
			to_exchange.println("ADD " + identifier + " BOND SELL 1001 " + num_to_sell);
			sells_sent[0] += num_to_sell;
			identifier++;
		}
	}
}

