package server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class Authenticator {
	private static Map<String, String> pswdDb = new HashMap<String, String>();
	public static void init() {
		RandomAccessFile pswdFile = null;
		try {
			pswdFile = new RandomAccessFile(DFSServer.serverRoot+"pswd", "rw");
			if(pswdFile.length() == 0) {
				try {
					pswdFile.writeBytes("root\t" + hashPswd("root123" + "\n"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			String line = pswdFile.readLine();
			while(line != null) {
				String[] splitLine = line.split("\t");
				pswdDb.put(splitLine[0], splitLine[1]);
				line = pswdFile.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public static boolean authenticate(String user, String pswdHash) {
		if(Authenticator.pswdDb.get(user).compareTo(pswdHash) == 0) 
			return true;
		else
			return false;
	}
	public static void addUser(String user, String pswd) {
		String pswdHash = Authenticator.hashPswd(pswd);
		Authenticator.pswdDb.put(user, pswdHash);
	}
	private static String hashPswd(String input) {
		byte[] hash = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			hash = md.digest(input.getBytes());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hash.toString();
	}
}
