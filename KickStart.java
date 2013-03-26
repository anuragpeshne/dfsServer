import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import server.DFSServer;
import testerClient.DFSClient;


public class KickStart {

	public static void main(String[] args) {
		File DFSRoot = new File(System.getProperty("user.home") + "/DFSRoot/");
		if(!DFSRoot.exists()) {
			DFSRoot.mkdir();
		}
		Desktop server = Desktop.getDesktop();
		try {
			server.open(DFSRoot);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*DFSServer fileServer = new DFSServer();
		Thread serverDaemon = new Thread(fileServer);
		serverDaemon.start();
		
		DFSClient testerClient = new DFSClient();
		Thread client = new Thread(testerClient);
		client.start();*/
	}

}
