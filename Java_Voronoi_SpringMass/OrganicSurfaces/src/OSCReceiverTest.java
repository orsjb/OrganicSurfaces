import java.io.IOException;
import java.net.SocketAddress;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;


public class OSCReceiverTest {

	public static void main(String[] args) throws IOException {
		OSCServer server = OSCServer.newUsing(OSCServer.UDP, 5000);
		server.addOSCListener(new OSCListener() {
			public void messageReceived(OSCMessage message, SocketAddress src, long timestamp) {
				System.out.println("Message from [" + src + "] (" + timestamp + ") : " + message);
			}
		});
		server.start();
		while(true);
	}
}
