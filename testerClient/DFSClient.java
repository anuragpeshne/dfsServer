package testerClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class DFSClient implements Runnable {
	public void run() {
		Socket connectSock = null;
		try {
			connectSock = new Socket("10.0.0.2", 8080);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		InputStreamReader streamReader = null;
		try {
			streamReader = new InputStreamReader(connectSock.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(streamReader);
		
		String input = null;
		try {
			input = reader.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println(input);
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
