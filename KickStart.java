import server.DFSServer;
import testerClient.DFSClient;


public class KickStart {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DFSServer fileServer = new DFSServer();
		Thread serverDaemon = new Thread(fileServer);
		serverDaemon.start();
		
		DFSClient testerClient = new DFSClient();
		Thread client = new Thread(testerClient);
		client.start();
	}

}
