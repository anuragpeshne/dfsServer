package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
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
				String enteredHash = hashIt(pswd); 
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
	public static String hashIt(String input) {
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		m.reset();
		m.update(input.getBytes());
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1,digest);
		String hashtext = bigInt.toString(16);
		// Now we need to zero pad it if you actually want the full 32 chars.
		while(hashtext.length() < 32 ){
		  hashtext = "0"+hashtext;
		}
		return hashtext;
	}
	public static User addUser(String username, String pswd) {
		User retUser = null;
		File userFile = new File(Server.serverRoot + "Users/" + username);
		if(!userFile.exists()) {
			try {
				userFile.createNewFile();
				RandomAccessFile userRF = new RandomAccessFile(userFile, "rw");
				userRF.writeBytes(User.hashIt(pswd) + "\n");
				retUser = new User(username, hashIt(pswd), userRF);
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

	public String getFile(String filename) {
		String response = "";
		Integer perm = this.concernedFiles.get(filename);
		if(perm != null && perm > 0) {
			try {
				DFile requestedFile = new DFile(filename, "r");
				String buffer;
				while((buffer = requestedFile.readLine()) != null)
					response += buffer + "\n";
				requestedFile.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
			response = "403";
		return response;
	}

	public boolean canWrite(String filename) {
		if(this.concernedFiles.get(filename) >= 6) {			//either can write or is owner
			return true;
		}
		else
			return false;
	}

	public void writeContent(String filename, String content) {
		try {
			File tF = new File(DFile.filesRoot + filename);
			if(tF.exists())
				tF.delete();
			DFile targetFile = new DFile(tF, "rw");
			targetFile.writeBytes(content);
			targetFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	public String listDirectory(String dirName) {
		String list = "";
		Iterator<Entry<String, Integer>> it = this.concernedFiles.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>)it.next();
	        if(pairs.getValue() > 0)
	        	list += pairs.getKey() + ",";
	    }
		return list;
	}
}
