package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class User {
	private String username;
	private String pswdHash;
	private Map<String, Integer> concernedFiles;
	public static User authenticate(String username, String pswd) {
		User retVal = null;
		File userFile = new File(Server.serverRoot + "Users/" + username);
		if(userFile.exists()) {
			try {
				RandomAccessFile userRF = new RandomAccessFile(userFile, "r");
				String passwordHash = userRF.readLine();
				String enteredHash = hashPswd(pswd); 
				if(passwordHash.compareTo(enteredHash) == 0) {
					retVal = new User(username, passwordHash, userRF);
					userRF.close();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return retVal;
	}
	
	private User(String userName, String hash, RandomAccessFile userFile) {
		this.username = userName;
		this.pswdHash = hash;
		this.concernedFiles = new HashMap<String, Integer>();
		String ip;
		try {
			ip = userFile.readLine();
			while(ip != null) {
				String[] file = ip.split("\t");
				this.concernedFiles.put(file[0], Integer.parseInt(file[1]));
				ip = userFile.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String getUsername() {
		return this.username;
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
	public static User addUser(String username, String pswd) {
		User retUser = null;
		File userFile = new File(Server.serverRoot + "Users/" + username);
		if(!userFile.exists()) {
			try {
				userFile.createNewFile();
				RandomAccessFile userRF = new RandomAccessFile(userFile, "rw");
				userRF.writeBytes(User.hashPswd(pswd) + "\n");
				retUser = new User(username, hashPswd(pswd), userRF);
				userRF.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return retUser;
	}
	public void addPermission(String fileName, int perm) {
		this.concernedFiles.put(fileName, perm);
		PermManager.addPermission(fileName, this.username, perm);
	}
	public void writeToDisk() {
		File userFOld = new File(Server.serverRoot + "Users/" + this.username);
		userFOld.delete();
		File userF = new File(Server.serverRoot + "Users/" + this.username);
		try {
			userF.createNewFile();
			RandomAccessFile userRF = new RandomAccessFile(userF, "rw");
			userRF.writeBytes(this.pswdHash + "\n");
			Iterator<Entry<String, Integer>> navi = this.concernedFiles.entrySet().iterator();
			while(navi.hasNext()) {
				Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>)navi.next();
		        userRF.writeBytes(pairs.getKey() + "\t" + pairs.getValue() + "\n");
			}
			userRF.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
