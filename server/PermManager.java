package server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class PermManager {
	public static final String permRoot = Server.serverRoot + "perms/";
	
	public static void addPermission(String fileName, String username, int perm) {
		RandomAccessFile permRF = null;
		try {
			permRF = new RandomAccessFile(permRoot + fileName, "rw");
			if(permRF.length() != 0)
				permRF.seek(permRF.length());
			permRF.writeBytes(username + "\t" + perm);
			permRF.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
