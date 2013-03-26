package server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
	public static final String serverRoot = System.getProperty("user.home") + "/DFSRoot/";
	
	public void init() {
		Server.createFS();
		Authenticator.init();
	}
	
	private static void createFS() {
		File DFSRoot = new File(System.getProperty("user.home") + "/DFSRoot/");
		if(!DFSRoot.exists()) {
			DFSRoot.mkdir();
			File filesDir = new File(serverRoot + "Files/");
			filesDir.mkdir();
			
			File usersDir = new File(serverRoot + "Users/");
			usersDir.mkdir();
			
			File permDir = new File(serverRoot + "perms/");
			permDir.mkdir();
		}
	}
	
	/*
	public void run() {
		ServerSocket listenSocket = null;
		try {
			listenSocket = new ServerSocket(8080);
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(true) {
			Socket serSock = null;
			try {
				serSock = listenSocket.accept();
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
		}
		//listenSocket.close();
	}*/
}