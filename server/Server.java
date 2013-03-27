package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class Server implements Runnable{
	public static final String serverRoot = System.getProperty("user.home") + "/DFSRoot/";
	
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
	
	public void run() {
		ServerSocket listenSocket = null;
		try {
			listenSocket = new ServerSocket(8080);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String input = "";
		while(input != null && input != "kill -9") {
			Socket serSock = null;
			try {
				serSock = listenSocket.accept();			//blocking is done here
			} catch (IOException e) {
				e.printStackTrace();
			}
			String response = "Hello! Greetings from server side";
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(serSock.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			writer.println(response);
			writer.close();
			try {
				InputStreamReader streamReader = new InputStreamReader(serSock.getInputStream());
				BufferedReader reader = new BufferedReader(streamReader);
				input = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		try {
			listenSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
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
