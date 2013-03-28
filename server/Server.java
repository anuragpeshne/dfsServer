package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;


public class Server {
	public static final String serverRoot = System.getProperty("user.home") + "/DFSRoot/";
	
	private static final int userTableSize = 20;
	private static Map<String, User> userTable = new LinkedHashMap<String, User>(userTableSize + 1, .75F, true){
		private static final long serialVersionUID = -2276838761335515485L;

		protected boolean removeEldestEntry(Map.Entry<String, User> eldest) {
		        return size() > userTableSize;
		     }
	};
	public class ClientHandler implements Runnable{
		BufferedReader reader;
		PrintWriter writer;
		Socket sock;
		
		public ClientHandler(Socket clientSocket) {
			this.sock = clientSocket;
			try {
				InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(isReader);
				
				writer = new PrintWriter(sock.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		@Override
		public void run() {
			String inMsg, outMsg = "501";				//501: Not Implemented
			try {
				inMsg = reader.readLine();
				String[] req = inMsg.split(" "); 
				if(req[0].compareTo("CONNECT") == 0) {
					outMsg = req[0];
					String response = Server.this.authenticateUser(req[1], req[2]);
					if(response != null)
						outMsg += " 200 " + response;		//200: OK
					else
						outMsg += " 401";					//401: Unauthorized
				}
				else if(req[0].compareTo("PING") == 0) {
					//implement ??
				}
				else if(req[0].compareTo("GET") == 0) {
					
				}
				else if(req[0].compareTo("PUT") == 0) {
					
				}
				writer.println(outMsg);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	public void init() {
		createFS();
	}
	

	private String authenticateUser(String username, String pswdHash) {
		String token = null;
		User authenticatedUser = User.authenticate(username, pswdHash);
		if(authenticatedUser != null) {
			Date d = new Date();
			String tokenIp = d.toString() + username;
			token = User.hashIt(tokenIp);
			Server.userTable.put(token, authenticatedUser);
		}
		return token;
	}


	private void createFS() {
		File DFSRoot = new File(System.getProperty("user.home") + "/DFSRoot/");
		if(!DFSRoot.exists()) {
			DFSRoot.mkdir();
			File filesDir = new File(serverRoot + "Files/");
			filesDir.mkdir();
			
			File usersDir = new File(serverRoot + "Users/");
			usersDir.mkdir();
			
			File permDir = new File(serverRoot + "perms/");
			permDir.mkdir();
			createRootUser();
		}
	}
	
	@SuppressWarnings("resource")
	public void start() {
		ServerSocket listenSocket = null;
		try {
			listenSocket = new ServerSocket(8080);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while(true) {
			try {
				Socket clientSocket = listenSocket.accept();
				
				Thread clientHandlerThread = new Thread(new ClientHandler(clientSocket));
				clientHandlerThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void createRootUser() {
		User rootUser = User.addUser("root", "root123");
		try {
			File testingF = new File(Server.serverRoot + "Files/" + "testing.txt");
			testingF.createNewFile();
			DFile testingFile = new DFile(testingF, "rw");
			testingFile.writeBytes("This is testing file.\nThis is owned by root");
			testingFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		rootUser.addPermission("testing.txt", 7);
		rootUser.writeToDisk();
	}
}
