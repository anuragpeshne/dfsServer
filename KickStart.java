import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import server.Server;
import testerClient.DFSClient;


public class KickStart {

	public static void main(String[] args) {
		Server server = new Server();
		server.init();
		/*DFSServer fileServer = new DFSServer();
		Thread serverDaemon = new Thread(fileServer);
		serverDaemon.start();
		
		DFSClient testerClient = new DFSClient();
		Thread client = new Thread(testerClient);
		client.start();*/
	}
	
}
