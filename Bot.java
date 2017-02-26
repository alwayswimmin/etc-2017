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
	public static int[] our_buy_price = new int[7];
	public static int[] our_sell_price = new int[7];
	public static int[] buys_sent = new int[7];
	public static int[] sells_sent = new int[7];
	public static double[] mid = new double[7];
	public static double[] mBuy = new double[7];
	public static double[] mSell = new double[7];
	public static double[] fair_price = new double[7];
	public static double[] spread = new double[7];
	public static int[] num_market_trades = new int[7];
	public static int[] limits = {100, 10, 10, 100, 100, 100, 100};

	public static TreeSet<Integer>[] buyIdentifiers = new TreeSet[7];
	public static TreeSet<Integer>[] sellIdentifiers = new TreeSet[7];
	public static TreeMap<Integer,Integer> sizeOfIdentifier = new TreeMap<Integer,Integer>();

	public static int usd = 0;

	public static int identifier;
	public static int action_buffer = 0;

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

			for(int i = 0; i < 7; i++) {
				buyIdentifiers[i] = new TreeSet<Integer>();
				sellIdentifiers[i] = new TreeSet<Integer>();
			}

			if(!reply.split(" ")[0].equals("HELLO")) {
				System.exit(0);
			}

			for(int i = 0; i <6; i++){
				// 				levels[i] = reply.substring(reply.indexOf(intToName(i))+reply.indexOf(intToName(i)).length(), reply.indexOf(" ", reply.indexOf(intToName(i))));
				levels[i] = Integer.parseInt(reply.substring(reply.indexOf(":", reply.indexOf(intToName(i)))+1, reply.indexOf(" ", reply.indexOf(intToName(i)))));
			}
			levels[6] =  Integer.parseInt(reply.substring(reply.indexOf(":", reply.indexOf(intToName(6)))+1));


			identifier = 1;

			executeTrades();
			while(true) {
				try {
					handleIncomingMessages();
					// executeTrades();
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
			int asset;
			switch(tokens[0]) {
				case "REJECT":
				case "OUT":
				case "ACK":
//					System.err.printf("The exchange replied: %s\n", message);
					break;
				case "TRADE":
					// int asset = nameToInt(tokens[1]);
					// int price = Integer.parseInt(tokens[2]);
					// int num = Integer.parseInt(tokens[3]);
					break;
				case "BOOK":
					asset = nameToInt(tokens[1]);
					int max_buy = 0;
					int min_sell = Integer.MAX_VALUE;
					boolean buyZone = true;
					for(int i = 2; i < tokens.length; i++) {
						if(tokens[i].equals("BUY")) {
							continue;
						}
						if(tokens[i].equals("SELL")) {
							buyZone = false;
							continue;
						}
						String[] arr = tokens[i].split(":");
						int x = Integer.parseInt(arr[0]);
						if(buyZone && x > max_buy) {
							max_buy = x;
						}
						if(!buyZone && x < min_sell) {
							min_sell = x;
						}
					}
					if(max_buy > 0 && min_sell < Integer.MAX_VALUE) {
						mid[asset] = (max_buy + min_sell) / 2.0;
						mBuy[asset] = max_buy;
						mSell[asset] = min_sell;
						spread[asset] = min_sell - max_buy;
					}
					break;
				case "FILL":
					System.err.printf("The exchange replied: %s\n", message);
					asset = nameToInt(tokens[2]);
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
				case "CLOSE":
					System.exit(0);
					break;
			}
			action_buffer++;
			if(action_buffer % 50 == 0) {
				executeTrades();
			}
		}
	}

	public static void executeTrades() {
		// bonds
		{
			int num_to_sell = limits[0]/2 + levels[0] - sells_sent[0];
			int num_to_buy = limits[0]/2 - levels[0] - buys_sent[0];
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

		// 	if(levels[nameToInt("VALE")] == 10 || levels[nameToInt("VALE")] == -10){
		// 			if(
		// 				to_exchange.println("CONVERT" + identifier + " BOND BUY 999 " + num_to_buy);
		// 		}
		// 		if(levels[nameToInt("VALBZ")] == 10 || levels[nameToInt("VALBZ")] == -10){
		// 			to_exchange.println("ADD " + identifier + " BOND BUY 999 " + num_to_buy);
		// 		}
		// 
		// 		if (mid[nameToInt("VALE")] < mid[nameToInt("VALBZ")]){
		// 
		// 		}
		// 		identifier++;
		if(levels[nameToInt("VALE")] == 10){
			to_exchange.println("CONVERT " + identifier + " VALE SELL " + 10);
			identifier++;
		}
		if(levels[nameToInt("VALE")] == -10){
			to_exchange.println("CONVERT " + identifier + " VALE BUY " + 10);
			identifier++;
		}
		if(levels[nameToInt("VALBZ")] == 10){
			to_exchange.println("CONVERT " + identifier + " VALE BUY " + 10);
			identifier++;
		}
		if(levels[nameToInt("VALBZ")] == -10){
			to_exchange.println("CONVERT " + identifier + " VALE SELL " + 10);
			identifier++;
		}

		if(levels[nameToInt("VALE")] != 10 || levels[nameToInt("VALE")] != -10 && levels[nameToInt("VALBZ")] != 10 || levels[nameToInt("VALBZ")] != -10){
			if(mBuy[nameToInt("VALE")] > 1+  mSell[nameToInt("VALBZ")]){
				int num_to_trade = Math.min(10-levels[nameToInt("VALBZ")], 10+levels[nameToInt("VALE")]);
				to_exchange.println("ADD " + identifier + " VALE SELL " + ( (int) mBuy[nameToInt("VALE")]) + " " +  num_to_trade);
				identifier++;
				to_exchange.println("ADD " + identifier + " VALBZ BUY " + ( (int) mSell[nameToInt("VALBZ")]) + " " + num_to_trade);
				identifier++;
			}

			if(mBuy[nameToInt("VALBZ")] > 1 + mSell[nameToInt("VALBZ")]){
				int num_to_trade = Math.min(10-levels[nameToInt("VALE")], 10+levels[nameToInt("VALBZ")]);
				to_exchange.println("ADD " + identifier + " VALBZ SELL " + ( (int) mBuy[nameToInt("VALBZ")]) + " " +  num_to_trade);
				identifier++;
				to_exchange.println("ADD " + identifier + " VALE BUY " + ( (int) mSell[nameToInt("VALE")]) + " " + num_to_trade);
				identifier++;
			}
			to_exchange.println("ADD " + identifier + " VALE SELL " + ( (int) mid[nameToInt("VALBZ")]+2) + " " + 2);
				identifier++;
			to_exchange.println("ADD " + identifier + " VALE BUY " + ( (int) mid[nameToInt("VALBZ")]-2) + " " +  2);
				identifier++;
		}

		
		double XLFmid =.3*mSell[nameToInt("BOND")] + .2*mSell[nameToInt("GS")] + .3*mSell[nameToInt("MS")]+ .2*mSell[nameToInt("WFC")];
		if((int)Math.ceil(XLFmid) != (int)Math.floor(XLFmid)){
			to_exchange.println("ADD " + identifier + " XLF SELL " + ((int)Math.ceil(XLFmid)+10) + " " + 2);
			identifier++;
			to_exchange.println("ADD " + identifier + " XLF BUY " + ((int)Math.floor(XLFmid)-10) + " " +  2);
			identifier++;
		}
		// Pennypinching or GS MS WFC
/*
		for(int stock = 3; stock <=5; stock++) {
			if(spread[stock]>= 2.9){
				int num_to_sell = limits[stock] / 2 + levels[stock] - sells_sent[stock];
				int num_to_buy = limits[stock] / 2 - levels[stock] - buys_sent[stock];
				int new_sell_price = ( (int) Math.round(mSell[stock]- 1) );
				int new_buy_price = ( (int) Math.round(mBuy[stock]+ 1) );
				if(new_sell_price != our_sell_price[stock]) {
					while(!sellIdentifiers[stock].isEmpty()) {
						int x = sellIdentifiers[stock].first();
						to_exchange.println("CANCEL " + x);
						num_to_sell += sizeOfIdentifier.get(x);
						sizeOfIdentifier.remove(x);
					}
				}
				if(new_buy_price != our_buy_price[stock]) {
					while(!buyIdentifiers[stock].isEmpty()) {
						int x = buyIdentifiers[stock].first();
						to_exchange.println("CANCEL " + x);
						num_to_buy += sizeOfIdentifier.get(x);
						sizeOfIdentifier.remove(x);
					}
				}
				if(num_to_buy > 0) {
					to_exchange.println("ADD " + identifier + " " + intToName(stock) + " BUY " +  new_buy_price + " " + num_to_buy);
					buyIdentifiers[stock].add(identifier);
					sizeOfIdentifier.put(identifier, num_to_buy);
					buys_sent[stock] += num_to_buy;
					identifier++;
				}
				if(num_to_sell > 0) {
					to_exchange.println("ADD " + identifier + " " +  intToName(stock) + " SELL " + new_sell_price + " "  + num_to_sell);
					sellIdentifiers[stock].add(identifier);
					sizeOfIdentifier.put(identifier, num_to_sell);
					sells_sent[stock] += num_to_sell;
					identifier++;
				}
			}
		}
*/
	}
}

