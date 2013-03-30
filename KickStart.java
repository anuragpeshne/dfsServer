
import server.Server;
import testerClient.DFSClient;


public class KickStart {

	public static void main(String[] args) {
		Server server = new Server();
		server.init();
		server.start();
		
		/*DFSClient testerClient = new DFSClient();
		Thread client = new Thread(testerClient);
		client.start();*/
	}
	
}
