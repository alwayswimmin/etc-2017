import java.lang.*;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;

public class Bot
{
    public static void main(String[] args)
    {
        try
        {
            Socket skt = new Socket("test-exch-same", 20000);
            BufferedReader from_exchange = new BufferedReader(new InputStreamReader(skt.getInputStream()));
            PrintWriter to_exchange = new PrintWriter(skt.getOutputStream(), true);

            to_exchange.println("HELLO SAME");
            String reply = from_exchange.readLine().trim();
            System.err.printf("The exchange replied: %s\n", reply);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
        }
    }
}
