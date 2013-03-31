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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;


public class Server {
	public static final String serverRoot = System.getProperty("user.home") + "/DFSRoot/";
	
	private static final int userTableSize = 20;
	private static Map<String, User> userTable = new LinkedHashMap<String, User>(userTableSize + 1, .75F, true){
		private static final long serialVersionUID = -2276838761335515485L;

		protected boolean removeEldestEntry(Map.Entry<String, User> eldest) {
		        return size() > userTableSize;
		     }
	};
	private Map<User, PrintWriter> writerMap;
	public static void main(String[] args) {
		Server server = new Server();
		server.init();
		server.start();
	}
	
	public Server() {
		this.writerMap = new HashMap<User, PrintWriter>();
	}
	
	public class ClientHandler implements Runnable{
		BufferedReader reader;
		PrintWriter writer;
		Socket sock;
		String clientUsername;
		
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
				while( (inMsg = reader.readLine()) != null) {
					String[] req = inMsg.split(" "); 
					outMsg = req[0];
					if(req[0].compareTo("CONNECT") == 0) {
						this.clientUsername = req[1];
						String response = this.authenticateUser(req[1], req[2]);
						if(response != null)
							outMsg += " 200 " + response;		//200: OK
						else
							outMsg += " 401";					//401: Unauthorized
						writer.println(outMsg);
						writer.flush();
					}
					else if(req[0].compareTo("PING") == 0) {
						//implement ??
					}
					else if(req[0].compareTo("GET") == 0) {
						User tempUser = userTable.get(req[1]);
						if(tempUser == null)
							outMsg += " 401";
						else {
							String response = tempUser.getFile(req[2]);
							response += "$$EOF$$";
							outMsg += " 200";
							writer.println(outMsg);
							writer.flush();
							writer.println(response);
							writer.flush();
						}
					}
					else if(req[0].compareTo("PUT") == 0) {
						User tempUser = userTable.get(req[1]);
						if(tempUser == null) {
							outMsg += " 401";
							writer.println(outMsg);
							writer.flush();
						}
						else {
							if(tempUser.canWrite(req[2])) {
								outMsg += " 200";
								writer.println(outMsg);
								writer.flush();
								String buffer, content = "";
								while((buffer = reader.readLine()).compareTo("$$EOF$$") != 0) {
									content += buffer + "\n";
								}
								tempUser.writeContent(req[2], content);
								Integer perm = tempUser.getPermission(req[2]);
								if(perm == null)
									perm = 7;
								PermManager.addPermission(req[2], tempUser.getUsername(), perm);
								tempUser.addPermission(req[2], perm);
								tempUser.writeToDisk();
								callBack(req[2]);
								writer.println("200");
								writer.flush();
							}
							else {
								outMsg += " 403";
								writer.println(outMsg);
								writer.flush();
							}
							
						}
					}
					else if(req[0].compareTo("LIST") == 0) {
						User tempUser =  userTable.get(req[1]);
						if(tempUser == null) {
							outMsg += " 401";
							writer.println(outMsg);
							writer.flush();
						}
						else {
							String list = tempUser.listDirectory(req[2]);
							outMsg += " 200";
							writer.println(outMsg);
							writer.flush();
							writer.println(list);
							writer.println("$$EOF$$");
							writer.flush();
						}
					}
					else if(req[0].compareTo("DEL") == 0) {
						User tempUser =  userTable.get(req[1]);
						if(tempUser == null) {
							outMsg += " 401";
							writer.println(outMsg);
							writer.flush();
						}
						else {
							tempUser.deleteFile(req[2]);
							writer.println(req[0] + " 200");
							writer.flush();
						}
					}
					else if(req[0].compareTo("PERMIT") == 0) {
						User tempUser =  userTable.get(req[1]);
						if(tempUser == null) {
							outMsg += " 401";
							writer.println(outMsg);
							writer.flush();
						}
						else {
							String response = tempUser.changePermission(req[2],req[3],req[4]);
							outMsg += " " + response;
							writer.println(outMsg);
							writer.flush();
						}
					}
					else {
						System.out.println("Unknown req:" + req);
						writer.println(outMsg);
						writer.flush();
					}
				}
			} catch(java.net.SocketException e) {
				System.out.println("User " + this.clientUsername + " droped");
			}catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(this.clientUsername + " disconnected");
		}
		
		private String authenticateUser(String username, String pswdHash) {
			String token = null;
			User authenticatedUser = User.authenticate(username, pswdHash);
			if(authenticatedUser != null) {
				Date d = new Date();
				String tokenIp = d.toString() + username;
				token = User.hashIt(tokenIp);
				Server.userTable.put(token, authenticatedUser);
				writerMap.put(authenticatedUser, writer);
			}
			return token;
		}
	}
	public void init() {
		createFS();
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
				System.out.println("got a connnection @" + clientSocket.getRemoteSocketAddress());
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
	
	private void callBack(String filename) {
		Iterator<Entry<String, User>> navi = Server.userTable.entrySet().iterator();
		while(navi.hasNext()) {
			Map.Entry<String, User> pairs = (Map.Entry<String, User>)navi.next();
	        User tempUser = pairs.getValue();
	        if(tempUser.isConcerned(filename)) {
	        	PrintWriter wr = writerMap.get(tempUser);
	        	wr.println("UPDATED " + filename);
	        }
		}
	}
}
